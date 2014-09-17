package net.rizon.moo.plugin.commands;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;

import java.util.HashSet;
import java.util.LinkedList;

class message249 extends Message
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
		CommandSlackers.opers.add(oper);
	}
}

class message219_s extends Message
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
		if (message366.waiting_on.isEmpty() && message366.command_source != null)
			Moo.sock.write("NAMES " + message366.command_source.getTargetName());
	}
}

class message353 extends Message
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
				CommandSlackers.opers.remove(name);
		}
	}
}

class message366 extends Message
{
	public message366()
	{
		super("366");
	}

	public static CommandSource command_source;
	public static HashSet<String> waiting_on = new HashSet<String>();

	@Override
	public void run(String source, String[] message)
	{
		if (command_source == null)
			return;
		
		if (CommandSlackers.opers.isEmpty())
			command_source.reply("There are no opers missing from " + command_source.getTargetName());
		else
		{
			command_source.reply("There are " + CommandSlackers.opers.size() + " opers missing from " + command_source.getTargetName() + ":");
			String operbuf = "";
			for (int i = 0; i < CommandSlackers.opers.size(); ++i)
			{
				operbuf += " " + CommandSlackers.opers.get(i);
				if (operbuf.length() > 200)
				{
					command_source.reply(operbuf.substring(1));
					operbuf = "";
				}
			}
			if (operbuf.isEmpty() == false)
				command_source.reply(operbuf.substring(1));
		}
		
		CommandSlackers.opers.clear();
		command_source = null;
		waiting_on.clear();
	}
}

class CommandSlackers extends Command
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

	public CommandSlackers(Plugin pkg)
	{
		super(pkg, "!SLACKERS", "Find opers online but not in the channel");
		
		this.requiresChannel(Moo.conf.getList("staff_channels"));
		this.requiresChannel(Moo.conf.getList("oper_channels"));
		this.requiresChannel(Moo.conf.getList("admin_channels"));
	}
	
	@Override
	public void onHelp(CommandSource source)
	{
		source.reply("Syntax: !SLACKERS");
		source.reply("Searches for all online opers who are not in this channel.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		opers.clear();
		message366.waiting_on.clear();
		for (Server s : Server.getServers())
			if (s.getSplit() == null && !s.isServices())
			{
				Moo.sock.write("STATS p " + s.getName());
				message366.waiting_on.add(s.getName());
			}
		message366.command_source = source;
	}
}