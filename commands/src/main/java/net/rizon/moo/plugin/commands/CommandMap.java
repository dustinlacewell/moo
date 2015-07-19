package net.rizon.moo.plugin.commands;

import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;

class message211 extends Message
{
	public message211()
	{
		super("211");
	}

	/*
	 * 0: moo
	 * 1: services.rizon.net[unknown@255.255.255.255]
	 * 2: 0  // Buf length
	 * 3: 24 // send.messages
	 * 4: 1  // send.bytes
	 * 5: 40 // recv.messages
	 * 6: 2  // recv.bytes
	 * 7: 48 0 TS GLN TBURST SVS UNKLN KLN KNOCK ENCAP CHW IE EX TS6 EOB QS
	 */
	@Override
	public void run(String source, String[] message)
	{
		long bytes = Long.parseLong(message[2]);
		Server serv = Server.findServerAbsolute(source);
		if (serv == null)
			serv = new Server(source);
		serv.bytes += bytes;
	}
}

class message219 extends Message
{
	public message219()
	{
		super("219");
	}

	private String convertBytes(long b)
	{
		String what = "bytes";

		if (b > 1024L)
		{
			b /= 1024L;
			what = "KB";
		}
		if (b > 1024L)
		{
			b /= 1024L;
			what = "MB";
		}
		if (b > 1024L)
		{
			b /= 1024L;
			what = "GB";
		}
		if (b > 1024L)
		{
			b /= 1024L;
			what = "TB";
		}

		String tmp = Long.toString(b);
		int dp = tmp.indexOf('.');
		if (tmp.length() > dp + 2)
			return tmp.substring(0, dp + 3) + " " + what;
		else
			return b + " " + what;
	}

	protected static CommandSource source;
	public static boolean request_all = false;

	@Override
	public void run(String source, String[] message)
	{
		Server serv = Server.findServerAbsolute(source);
		if (serv == null || message219.source == null || message[1].equals("?") == false)
			return;
		else if (request_all || serv.bytes >= 1024)
			message219.source.reply("[MAP] " + source + " " + this.convertBytes(serv.bytes));
	}
}

class message265 extends Message
{
	public message265()
	{
		super("265");
	}

	protected static CommandSource source;
	public static int request_users = 0;
	private static Pattern p = Pattern.compile("Current local users:? (\\d+)[ ,]*[Mm]ax:? (\\d+)");

	@Override
	public void run(String source, String[] message)
	{
		if (message265.source == null || message.length < 2)
			return;
		
		Matcher m = p.matcher(message[message.length - 1]);
		m.matches();		
		int users = Integer.parseInt(m.group(1));		

		if (users >= request_users)
			message265.source.reply("[MAP] " + source + " " + users);
	}
}

class commandMapBase extends Command
{
	private boolean full;

	public commandMapBase(Plugin pkg, final String cmd, boolean full)
	{
		super(pkg, cmd, "View hub lag and routing information");
		this.full = full;

		this.requiresChannel(Moo.conf.staff_channels);
		this.requiresChannel(Moo.conf.oper_channels);
		this.requiresChannel(Moo.conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: " + this.getCommandName() + " [{ usercount | HUB server.name | FIND mask}]");
		source.notice("Searches for information about servers.");
		source.notice("Without any further arguments, the sendq (in bytes) of hubs is shown.");
		if(!this.getCommandName().equalsIgnoreCase("!MAP-"))
			source.notice("The sendq output will be hidden unless it exceeds 1023 bytes, use !MAP- to see them.");
		source.notice("If a user count is given, only servers and their user count with that amount of");
		source.notice("(or more) users will be shown.");
		source.notice("HUB server.name shows what other servers server.name is connected to.");
		source.notice("FIND mask tries to find all servers matching the given mask.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length == 1)
		{
			for (Server s : Server.getServers())
				if (s.isHub())
				{
					s.bytes = 0;
					Moo.write("STATS", "?", s.getName());
				}
			message219.request_all = this.full;
			message219.source = source;
		}
		else if (params.length > 1)
		{
			if (params[1].equalsIgnoreCase("HUB") && params.length > 2)
			{
				Server s = Server.findServer(params[2]);
				if (s == null)
					source.reply("[MAP] Server " + params[2] + " not found");
				else
					for (Iterator<Server> it = s.links.iterator(); it.hasNext();)
						source.reply("[MAP] " + s.getName() + " is linked to " + it.next().getName());
			}
			else if (params[1].equalsIgnoreCase("FIND") && params.length > 2)
			{
				int count = 0;
				for (Server s : Server.getServers())
				{
					if (Moo.matches(s.getName(), "*" + params[2] + "*"))
					{
						source.reply("[MAP] Server " + s.getName() + " matches " + params[2]);
						++count;
					}
				}
				source.reply("[MAP] End of match, " + count + " servers found");
			}
			else
			{
				try
				{
					int users = Integer.parseInt(params[1]);

					for (Server s : Server.getServers())
						Moo.write("USERS", s.getName());

					message265.source = source;
					message265.request_users = users;
				}
				catch (NumberFormatException ex)
				{
					Server s = Server.findServer(params[1]);
					if (s == null)
						source.reply("[MAP] Server " + params[1] + " not found");
					else
					{
						s.bytes = 0;
						Moo.write("STATS", "?", s.getName());;
						message219.request_all = this.full;
						message219.source = source;
					}
				}
			}
		}
	}
}

class commandMapRegular extends commandMapBase
{
	public commandMapRegular(Plugin pkg)
	{
		super(pkg, "!MAP", false);
	}
}

class commandMapAll extends commandMapBase
{
	public commandMapAll(Plugin pkg)
	{
		super(pkg, "!MAP-" , true);
	}
}

class CommandMap
{
	private message211 msg_211 = new message211();
	private message219 msg_219 = new message219();
	private message265 msg_265 = new message265();
	private commandMapRegular map_reg;
	private commandMapAll map_all;

	public CommandMap(Plugin pkg)
	{
		this.map_reg = new commandMapRegular(pkg);
		this.map_all = new commandMapAll(pkg);
	}

	public void remove()
	{
		this.msg_211.remove();
		this.msg_219.remove();
		this.msg_265.remove();

		this.map_reg.remove();
		this.map_all.remove();
	}
}
