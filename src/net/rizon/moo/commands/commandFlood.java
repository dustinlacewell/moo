package net.rizon.moo.commands;

import java.util.regex.*;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.Vector;

import net.rizon.moo.command;
import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;

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
		else if (server.lastSplit + 240 > System.currentTimeMillis() / 1000L)
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
		Date then = new Date(System.currentTimeMillis() - (30 * 1000)); // 30 seconds ago
		
		if (server.last_link != null && server.last_link.after(then))
			return;
		else if (server.last_split != null && server.last_split.after(then))
			return;

		floodData d = new floodData();
		d.nick = nick;
		d.ident = ident;
		d.host = host;
		d.when = new Date();
		
		if (floodTime > 0 && System.currentTimeMillis() > floodTime)
		{
			floodTime = 0;
			for (int c = 0; c < moo.conf.getAdminChannels().length; ++c)
				moo.privmsg(moo.conf.getAdminChannels()[c], "End of flood - found " + (floodData.isEmpty() == false ? floodData.lastElement().size() : 0) + " matches");
		}
		
		if (floodTime == 0 && recentConnects.size() == 50 && recentConnects.getFirst().when.after(then))
		{
			for (int c = 0; c < moo.conf.getAdminChannels().length; ++c)
				moo.privmsg(moo.conf.getAdminChannels()[c], "Flood detected - (50 in 30s) *** collecting matching nick/ident/gecos for next 60s");
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

	public commandFlood(mpackage pkg)
	{
		super(pkg, "!FLOOD", "Manage flood lists");
		this.requireAdmin();
	}

	public void execute(String source, String target, String[] params)
	{
		if (params.length == 1)
		{
			moo.notice(source, "Flood commands:");
			moo.notice(source, "!flood <flood list number> list -- Displays all entries in a flood list");
			moo.notice(source, "!flood <flood list number> del [host/range] -- Deletes selected entries from a flood list or the entire list");
			moo.notice(source, "!flood <flood list number> akill [duration] --  Akills the entire list. If no duration is given, 2d will be assumed");
			moo.notice(source, "!flood <flood list number> apply <regex> -- Delete all entries that don't match the given regex matched against nick");
			moo.notice(source, "!flood list -- Lists all available flood lists");
			return;
		}
		else if (params[1].equalsIgnoreCase("LIST"))
		{
			if (floodManager.listSize() == 0)
				moo.privmsg(target, "There are no flood lists.");
			else
				for (int i = 0; i < floodManager.listSize(); ++i)
				{
					LinkedList<floodData> fd = floodManager.getList(i);
					moo.reply(source, target, (i + 1) + ": Contains " + fd.size() + " entries, last entry at: " + fd.getLast().when);
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
			moo.notice(source, "Invalid flood list number");
			return;
		}
		
		LinkedList<floodData> data = floodManager.getList(i - 1);
		if (data == null)
		{
			moo.notice(source, "There is no flood list numbered " + i);
			return;
		}
		
		if (params[2].equalsIgnoreCase("LIST"))
		{
			for (int j = 0; j < data.size(); ++j)
			{
				floodData fd = data.get(j);
				moo.notice(source, (j + 1) + ": " + fd.nick + " (" + fd.ident + "@" + fd.host + ")");
			}
			moo.notice(source, "End of flood list, " + data.size() + " entries");
		}
		else if (params[2].equalsIgnoreCase("DEL"))
		{
			if (params.length > 3)
			{				
				if (Character.isDigit(params[3].charAt(0)) && !params[3].contains("."))
				{
					// A valid range argument would be 1-5,9,10,15. Duplicates should be allowed.
					String[] parts = params[3].split(",");
					TreeSet<Integer> tobedeleted = new TreeSet<Integer>();
					for (int k = 0; k < parts.length; k++)
					{
						int dashpos = parts[k].indexOf('-');
						
						if (dashpos == -1) // No range, just a single integer
						{
							int tmp = 0;
							try
							{
								tmp = Integer.valueOf(parts[k]);
							}
							catch (NumberFormatException ex)
							{
								if (moo.conf.getDebug() > 0)
									System.out.println("Invalid number: " + parts[k]);
								
								continue;
							}
							tobedeleted.add(tmp - 1);
						}
						else
						{
							int min = 0;
							int max = 0;
							try
							{
								min = Integer.valueOf(parts[k].substring(0, dashpos));
								max = Integer.valueOf(parts[k].substring(dashpos+1, parts[k].length()));
							}
							catch (NumberFormatException ex)
							{
								if (moo.conf.getDebug() > 0)
									System.out.println("Invalid number[R]: " + min + " " + max);
								
								continue;
							}
							for ( ; min <= max; min++)
								tobedeleted.add(min - 1);
						}
					}
					if (tobedeleted == null || tobedeleted.isEmpty())
					{
						moo.notice(source, "Nothing to be deleted.");
						return;
					}
					
					int deleted = 0;
					for (Iterator<Integer> ii = tobedeleted.descendingIterator(); ii.hasNext();)
					{
						int del = ii.next(); // HACK: A temporary variable seems to be required
						data.remove(del);
						deleted++;
					}
					
					moo.notice(source, "Deleted " + deleted + " entries");
					return;
				}
				
				boolean match = false;
				for (Iterator<floodData> it = data.iterator(); it.hasNext();)
				{
					floodData d = it.next();
					if (moo.match(d.host, params[3]))
					{
						moo.notice(source, "Removed flood entry " + d.host);
						it.remove();
						match = true;
					}
				}
				if (match == false)
					moo.notice(source, "No match for " + params[3]);
			}
			else
			{
				data.clear();
				moo.reply(source, target, "Removed flood list " + i);
			}

			if (data.isEmpty() == true)
				floodManager.removeList(i - 1);
		}
		else if (params[2].equalsIgnoreCase("AKILL"))
		{
			String duration = "2d";
			if (params.length > 3)
			{
				if (params[3].startsWith("+"))
					params[3] = params[3].substring(1);
				
				if (params[3].endsWith("d") || params[3].endsWith("h")
						|| params[3].endsWith("m") || params[3].endsWith("s")
						|| Character.isDigit(params[3].charAt(params[3].length() - 1)))
				{
					duration = params[3];
				}
				else
				{
					moo.notice(source, "Invalid duration: " + params[3]);
					return;
				}
			}
			for (Iterator<floodData> it = data.iterator(); it.hasNext();)
			{
				floodData d = it.next();
				moo.akill(d.host, "+" + duration, "Possible flood bot (" + d.nick + ")");
			}

			moo.reply(source, target, "Akilled " + data.size() + " entries");
			floodManager.removeList(i - 1);
		}
		else if (params[2].equalsIgnoreCase("APPLY"))
		{
			Pattern regex;
			try
			{
				regex = Pattern.compile(params[3]);
			}
			catch (PatternSyntaxException ex)
			{
				moo.notice(source, "Invalid regex: " + params[3]);
				return;
			}
			
			int deleted = 0;
			for (Iterator<floodData> it = data.iterator(); it.hasNext();)
			{
				if (regex.matcher(it.next().nick).matches())
					continue;
				
				it.remove();
				deleted++;
			}
			if (data.isEmpty())
			{
				floodManager.removeList(i - 1);
				moo.notice(source, "All entries removed. Deleted list " + i);
				return;
			}
			moo.notice(source, "Removed " + deleted + " entries. " + data.size() + " entries remain.");
		}
	}
}
