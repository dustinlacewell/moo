package net.rizon.moo.commands;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import net.rizon.moo.command;
import net.rizon.moo.message;
import net.rizon.moo.moo;

class messageePrivmsg extends message
{
	public messageePrivmsg()
	{
		super("PRIVMSG");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 2 || message[0].equalsIgnoreCase("#debug") == false || message[1].startsWith("Ident/Gecos/Nick") == false)
			return;
		
		String[] tokens = message[1].split(" ");
		
		if (tokens.length < 3)
			return;

		int ex = tokens[2].indexOf('!'), at = tokens[2].indexOf('@'), co = tokens[2].indexOf(':');
		if (ex == -1 || at == -1 || co == -1)
			return;
		
		String nick = tokens[2].substring(0, ex), ident = tokens[2].substring(ex + 1, at), host = tokens[2].substring(at + 1, co);
		
		if (nick.startsWith("ESP|") || nick.startsWith("MEX|") || nick.startsWith("BGR|") || nick.startsWith("ARG|"))
			moo.akill(host, "+5d", "Compromised host ( " + nick + ")");
		
		floodManager.add(nick, ident, host);
	}
}

class floodData
{
	public String nick;
	public String ident;
	public String host;
	public Date when;
}

class floodManager
{
	private static Vector<LinkedList<floodData>> floodData = new Vector<LinkedList<floodData>>();
	private static LinkedList<floodData> recentConnects = new LinkedList<floodData>();
	
	private static long floodTime = 0;
	private static boolean floodNew = false;

	public static void add(final String nick, final String ident, final String host)
	{
		floodData d = new floodData();
		d.nick = nick;
		d.ident = ident;
		d.host = host;
		d.when = new Date();
		
		Date then = new Date(System.currentTimeMillis() - (30 * 1000)); // 30 seconds ago
		
		if (floodTime > 0 && System.currentTimeMillis() > floodTime)
		{
			floodTime = 0;
			if (moo.conf.getChannels() != null)
				for (int c = 0; c < moo.conf.getChannels().length; ++c)
					moo.sock.privmsg(moo.conf.getChannels()[c], "End of flood - found " + (floodData.isEmpty() == false ? floodData.lastElement().size() : 0) + " matches");
		}
		
		if (floodTime == 0 && recentConnects.size() == 50 && recentConnects.getFirst().when.after(then))
		{
			if (moo.conf.getChannels() != null)
				for (int c = 0; c < moo.conf.getChannels().length; ++c)
					moo.sock.privmsg(moo.conf.getChannels()[c], "Flood detected - (50 in 30s) *** collecting matching nick/ident/gecos for next 60s");
			floodTime = System.currentTimeMillis() + (60 * 1000);
			floodNew = true;
		}
		
		if (floodTime > 0)
		{
			LinkedList<floodData> fd = null;
			if (floodData.isEmpty() || floodNew)
			{
				floodNew = false;
				fd = new LinkedList<floodData>();
				floodData.add(fd);
				
				for (Iterator<floodData> it = recentConnects.iterator(); it.hasNext();)
					fd.add(it.next());
				recentConnects.clear();
			}
			else
				fd = floodData.lastElement();
			
			fd.add(d);
		}
		else
		{	
			if (recentConnects.size() >= 50)
				recentConnects.removeFirst();
			recentConnects.add(d);
		}
	}
	
	public static LinkedList<floodData> getList(int num)
	{
		try
		{
			return floodData.elementAt(num);
		}
		catch (ArrayIndexOutOfBoundsException ex)
		{
			return null;
		}
	}
	
	public static void removeList(int num)
	{
		try
		{
			floodData.remove(num);
		}
		catch (ArrayIndexOutOfBoundsException ex)
		{
		}
	}
	
	public static int listSize()
	{
		return floodData.size();
	}
	
	public static void clear()
	{
		floodData.clear();
		floodNew = false;
		floodTime = 0;
	}
}

public class commandFlood extends command
{
	@SuppressWarnings("unused")
	private static messageePrivmsg nm = new messageePrivmsg();

	public commandFlood()
	{
		super("!FLOOD");
	}

	public void execute(String source, String target, String[] params)
	{
		if (params.length == 1)
		{
			moo.sock.notice(source, "Flood commands:");
			moo.sock.notice(source, "!flood <flood list number> list -- Displays all entries in a flood list");
			moo.sock.notice(source, "!flood <flood list number> del [hos]t -- Deletes selected entry from a flood list or the entire list");
			moo.sock.notice(source, "!flood <flood list number> akill --  Akills the entire list");
			moo.sock.notice(source, "!flood list -- Lists all available flood lists");
			return;
		}
		else if (params[1].equalsIgnoreCase("LIST"))
		{
			if (floodManager.listSize() == 0)
				moo.sock.privmsg(target, "There are no flood lists.");
			else
				for (int i = 0; i < floodManager.listSize(); ++i)
				{
					LinkedList<floodData> fd = floodManager.getList(i);
					moo.sock.privmsg(target, (i + 1) + ": Contains " + fd.size() + " entries, last entry at: " + fd.getLast().when);
				}
			return;
		}
		
		int i;
		try
		{
			i = Integer.parseInt(params[1]);
		}
		catch (NumberFormatException ex)
		{
			moo.sock.notice(source, "Invalid flood list number");
			return;
		}
		
		LinkedList<floodData> data = floodManager.getList(i - 1);
		if (data == null)
		{
			moo.sock.privmsg(target, "There is no flood list numbered " + i);
			return;
		}
		
		if (params[2].equalsIgnoreCase("LIST"))
		{
			for (int j = 0; j < data.size(); ++j)
			{
				floodData fd = data.get(j);
				moo.sock.privmsg(target, (j + 1) + ": " + fd.nick + " (" + fd.ident + "@" + fd.host + ")");
			}
			moo.sock.privmsg(target, "End of flood list, " + data.size() + " entries");
		}
		else if (params[2].equalsIgnoreCase("DEL"))
		{
			if (params.length > 3)
			{
				boolean match = false;
				for (Iterator<floodData> it = data.iterator(); it.hasNext();)
				{
					floodData d = it.next();
					if (moo.match(d.host, params[3]))
					{
						moo.sock.privmsg(target, "Removed flood entry " + d.host);
						it.remove();
						match = true;
					}
				}
				if (match == false)
					moo.sock.privmsg(target, "No match for " + params[3]);
			}
			else
			{
				data.clear();
				moo.sock.privmsg(target, "Removed flood list " + i);
			}

			if (data.isEmpty() == true)
				floodManager.removeList(i - 1);
		}
		else if (params[2].equalsIgnoreCase("AKILL"))
		{
			for (Iterator<floodData> it = data.iterator(); it.hasNext();)
			{
				floodData d = it.next();
				moo.akill(d.host, "+2d", "Possible flood bot (" + d.nick + ")");
			}
		}
	}
}
