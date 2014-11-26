package net.rizon.moo.plugin.commands;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import net.rizon.moo.Channel;
import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;
import net.rizon.moo.User;

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

		CommandSlackers.waiting_on.remove(source);
		if (CommandSlackers.waiting_on.isEmpty() && CommandSlackers.command_source != null)
		{
			Channel c = Moo.channels.find(CommandSlackers.command_source.getTargetName());

			for (Iterator<String> it = CommandSlackers.opers.iterator(); it.hasNext(); )
			{
				String user = it.next();
				User u = Moo.users.find(user);

				if (c != null && u != null && c.findUser(u) != null)
					it.remove();
			}

			if (CommandSlackers.opers.isEmpty())
				CommandSlackers.command_source.reply("There are no opers missing from " + CommandSlackers.command_source.getTargetName());
			else
			{
				CommandSlackers.command_source.reply("There are " + CommandSlackers.opers.size() + " opers missing from " + CommandSlackers.command_source.getTargetName() + ":");
				String operbuf = "";
				for (int i = 0; i < CommandSlackers.opers.size(); ++i)
				{
					operbuf += " " + CommandSlackers.opers.get(i);
					if (operbuf.length() > 200)
					{
						CommandSlackers.command_source.reply(operbuf.substring(1));
						operbuf = "";
					}
				}
				if (operbuf.isEmpty() == false)
					CommandSlackers.command_source.reply(operbuf.substring(1));
			}

			CommandSlackers.opers.clear();
			CommandSlackers.command_source = null;
			CommandSlackers.waiting_on.clear();
		}
	}
}

class CommandSlackers extends Command
{
	@SuppressWarnings("unused")
	private static message249 msg_249 = new message249();
	@SuppressWarnings("unused")
	private static message219_s msg_219 = new message219_s();

	public static LinkedList<String> opers = new LinkedList<String>();
	public static CommandSource command_source;
	public static HashSet<String> waiting_on = new HashSet<String>();

	public CommandSlackers(Plugin pkg)
	{
		super(pkg, "!SLACKERS", "Find opers online but not in the channel");

		this.requiresChannel(Moo.conf.staff_channels);
		this.requiresChannel(Moo.conf.oper_channels);
		this.requiresChannel(Moo.conf.admin_channels);
	}

	@Override
	public void remove()
	{
		super.remove();
		msg_219.remove();
		msg_249.remove();
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
		waiting_on.clear();
		for (Server s : Server.getServers())
			if (s.getSplit() == null && !s.isServices())
			{
				Moo.sock.write("STATS p " + s.getName());
				waiting_on.add(s.getName());
			}
		command_source = source;
	}
}