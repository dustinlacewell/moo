package net.rizon.moo.plugin.servermonitor;

import java.util.concurrent.TimeUnit;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;

class SCheckTimer implements Runnable
{
	protected static final int delay = 70;

	private Server server;
	private int port;
	private boolean ssl;
	private String[] targets;
	private boolean use_v6;

	public SCheckTimer(final Server server, final String[] targets, boolean ssl, int port, boolean use_v6)
	{
		this.server = server;
		this.targets = targets;
		this.ssl = ssl;
		this.port = port;
		this.use_v6 = use_v6;
	}

	@Override
	public void run()
	{
		SCheck check = new SCheck(this.server, this.targets, this.ssl, this.port, true, this.use_v6);
		check.start();
	}
}

class SCheckEndTimer implements Runnable
{
	private CommandSource source;

	public SCheckEndTimer(CommandSource source)
	{
		this.source = source;
	}

	@Override
	public void run()
	{
		source.reply("[SCHECK] All server checks completed.");
	}
}

class CommandScheck extends Command
{
	public CommandScheck(Plugin pkg)
	{
		super(pkg, "!SCHECK", "Check if a server is online");

		this.requiresChannel(Moo.conf.staff_channels);
		this.requiresChannel(Moo.conf.oper_channels);
		this.requiresChannel(Moo.conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !SCHECK <server> [+port] [+6]");
		source.notice("Attempts to connect to the given server. If no port is given, 6667 is assumed.");
		source.notice("If port is prefixed with a +, SSL is used. If +6 is given, IPv6 instead of IPv4 will be used.");
		source.notice("Once a connection is established, the global user count, server count and uptime will be shown.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length == 1)
			source.reply("Syntax: !scheck <server> [port] [+6]");
		else
		{
			Server serv = Server.findServer(params[1]);
			if (serv == null && params[1].equalsIgnoreCase("ALL") == false)
				source.reply("[SCHECK] Server " + params[1] + " not found");
			else
			{
				int port = 6667;
				boolean ssl = false, use_v6 = false;
				int port_pos = 0;

				if (params.length > 2)
				{
					if (params[2].equals("+6") || (params.length > 3 && params[3].equals("+6")))
					{
						use_v6 = true;
						port_pos = (params[2].equals("+6")) ? 3 : 2;
					}
					else
						port_pos = 2;

					if (port_pos > 0 && port_pos < params.length)
					{
						String port_str = params[port_pos];
						if (port_str.startsWith("+"))
						{
							port_str = port_str.substring(1);
							ssl = true;
						}

						try
						{
							port = Integer.parseInt(port_str);
							if (port <= 0 || port > 65535)
								throw new NumberFormatException("Invalid port range");
						}
						catch (NumberFormatException ex)
						{
						}
					}
				}

				if (params[1].equalsIgnoreCase("ALL") == false)
				{
					SCheck check = new SCheck(serv, new String[] { source.getTargetName() }, ssl, port, false, use_v6);
					check.start();
				}
				else
				{
					int delay = 0;

					for (Server s : Server.getServers())
					{
						if (s.isHub() || s.isServices())
							continue;

						Moo.schedule(new SCheckTimer(s, new String[] { source.getTargetName() }, ssl, port, use_v6), delay++ * SCheckTimer.delay, TimeUnit.SECONDS);
					}

					Moo.schedule(new SCheckEndTimer(source), delay * SCheckTimer.delay, TimeUnit.SECONDS);

					source.reply("[SCHECK] Queued " + delay + " checks in the next " + (delay * SCheckTimer.delay) + " seconds");
				}
			}
		}
	}
}
