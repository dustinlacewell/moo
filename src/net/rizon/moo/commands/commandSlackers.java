package net.rizon.moo.commands;

import java.util.HashSet;
import java.util.LinkedList;

import net.rizon.moo.command;
import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
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

class message219_s extends message
{
	public message219_s()
	{
		super("219");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message[1].equals("p") == false)
			return;

		message366.waiting_on.remove(source);
		if (message366.waiting_on.isEmpty() && message366.target_chan != null)
			moo.sock.write("NAMES " + message366.target_chan);
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
			while (name.isEmpty() == false && (name.charAt(0) == '~' || name.charAt(0) == '&' || name.charAt(0) == '@' || name.charAt(0) == '%' || name.charAt(0) == '+'))
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
	
	public static String target_source = null;
	public static String target_chan = null;
	public static HashSet<String> waiting_on = new HashSet<String>();

	@Override
	public void run(String source, String[] message)
	{
		if (target_source == null || target_chan == null)
			return;
		
		if (commandSlackers.opers.isEmpty())
			moo.sock.reply(target_source, target_chan, "There are no opers missing from " + target_chan);
		else
		{
			moo.sock.reply(target_source, target_chan, "There are " + commandSlackers.opers.size() + " opers missing from " + target_chan + ":");
			String operbuf = "";
			for (int i = 0; i < commandSlackers.opers.size(); ++i)
			{
				operbuf += " " + commandSlackers.opers.get(i);
				if (operbuf.length() > 200)
				{
					moo.sock.reply(target_source, target_chan, operbuf.substring(1));
					operbuf = "";
				}
			}
			if (operbuf.isEmpty() == false)
				moo.sock.reply(target_source, target_chan, operbuf.substring(1));
		}
		
		commandSlackers.opers.clear();
		target_source = null;
		target_chan = null;
		waiting_on.clear();
	}
}

public class commandSlackers extends command
{	
	@SuppressWarnings("unused")
	private static message249 msg_249 = new message249();
	@SuppressWarnings("unused")
	private static message219_s msg_219 = new message219_s();
	@SuppressWarnings("unused")
	private static message353 msg_353 = new message353();
	@SuppressWarnings("unused")
	private static message366 msg_366 = new message366();
	
	public static LinkedList<String> opers = new LinkedList<String>();

	public commandSlackers(mpackage pkg)
	{
		super(pkg, "!SLACKERS", "Find opers online but not in the channel");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		opers.clear();
		message366.waiting_on.clear();
		for (server s : server.getServers())
			if (s.getSplit() == null && !s.isServices())
			{
				moo.sock.write("STATS p " + s.getName());
				message366.waiting_on.add(s.getName());
			}
		message366.target_chan = target;
		message366.target_source = source;
	}
}