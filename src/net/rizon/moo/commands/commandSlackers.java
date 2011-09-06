package net.rizon.moo.commands;

import java.util.Iterator;
import java.util.LinkedList;

import net.rizon.moo.command;
import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.server;

class message249 extends message
{
	public message249()
	{
		super("249");
	}

	@Override
	public void run(String source, String[] message)
	{
		String[] m = message[2].split(" ");
		if (m.length != 5)
			return;
		
		String oper = m[1];
		commandSlackers.opers.add(oper);
	}
}

class message353 extends message
{
	public message353()
	{
		super("353");
	}

	@Override
	public void run(String source, String[] message)
	{
		String[] names = message[3].split(" ");
		for (int i = 0; i < names.length; ++i)
		{
			String name = names[i];
			while (name.isEmpty() == false && Character.isLetter(name.charAt(0)) == false)
				name = name.substring(1);
			if (name.isEmpty() == false)
				commandSlackers.opers.remove(name);
		}
	}
}

class message366 extends message
{
	public message366()
	{
		super("366");
	}
	
	public static String target_chan = null;

	@Override
	public void run(String source, String[] message)
	{
		if (target_chan == null)
			return;
		
		if (commandSlackers.opers.isEmpty())
			moo.sock.privmsg(target_chan, "There are no opers missing from " + target_chan);
		else
		{
			moo.sock.privmsg(target_chan, "There are " + commandSlackers.opers.size() + " opers missing from " + target_chan + ":");
			String operbuf = "";
			for (int i = 0; i < commandSlackers.opers.size(); ++i)
			{
				operbuf += " " + commandSlackers.opers.get(i);
				if (operbuf.length() > 200)
				{
					moo.sock.privmsg(target_chan, operbuf.substring(1));
					operbuf = "";
				}
			}
			if (operbuf.isEmpty() == false)
				moo.sock.privmsg(target_chan, operbuf.substring(1));
		}
		
		commandSlackers.opers.clear();
	}
}

public class commandSlackers extends command
{	
	@SuppressWarnings("unused")
	private static message249 msg_249 = new message249();
	@SuppressWarnings("unused")
	private static message353 msg_353 = new message353();
	@SuppressWarnings("unused")
	private static message366 msg_366 = new message366();
	
	public static LinkedList<String> opers = new LinkedList<String>();

	public commandSlackers()
	{
		super("!SLACKERS", "Find opers online but not in the channel");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
			moo.sock.write("STATS p " + it.next().getName());
		moo.sock.write("NAMES " + target);
		message366.target_chan = target;
	}
}