package net.rizon.moo.servermonitor;

import java.util.Date;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;
import net.rizon.moo.timer;

class scheckTimer extends timer
{
	protected static final int delay = 70;
	
	private server server;
	private int port;
	private boolean ssl;
	private String[] targets;
	private boolean use_v6;
	
	public scheckTimer(int delay, final server server, final String[] targets, boolean ssl, int port, boolean use_v6)
	{
		super(delay * scheckTimer.delay, false);
		
		this.server = server;
		this.targets = targets;
		this.ssl = ssl;
		this.port = port;
		this.use_v6 = use_v6;
	}

	@Override
	public void run(Date now)
	{
		scheck check = new scheck(this.server, this.targets, this.ssl, this.port, true, this.use_v6);
		check.start();
	}
}

class scheckEndTimer extends timer
{
	private String source;
	private String target;
	
	public scheckEndTimer(int delay, final String source, final String target)
	{
		super(delay * scheckTimer.delay, false);
		
		this.source = source;
		this.target = target;
	}
	
	@Override
	public void run(Date now)
	{
		moo.reply(this.source, this.target, "[SCHECK] All server checks completed.");
	}
}

class commandScheck extends command
{
	public commandScheck(mpackage pkg)
	{
		super(pkg, "!SCHECK", "Check if a server is online");
	}
	
	@Override
	public void onHelp(String source)
	{
		moo.notice(source, "Syntax: !SCHECK <server> [+port] [+6]");
		moo.notice(source, "Attempts to connect to the given server. If no port is given, 6667 is assumed.");
		moo.notice(source, "If port is prefixed with a +, SSL is used. If +6 is given, IPv6 instead of IPv4 will be used.");
		moo.notice(source, "Once a connection is established, the global user count, server count and uptime will be shown.");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length == 1)
			moo.reply(source, target, "Syntax: !scheck <server> [port] [+6]");
		else
		{
			server serv = server.findServer(params[1]);
			if (serv == null && params[1].equalsIgnoreCase("ALL") == false)
				moo.reply(source, target, "[SCHECK] Server " + params[1] + " not found");
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
					scheck check = new scheck(serv, new String[] { target }, ssl, port, false, use_v6);
					check.start();
				}
				else
				{
					int delay = 0;
					
					for (server s : server.getServers())
					{
						if (s.isHub() || s.isServices())
							continue;
						
						new scheckTimer(delay++, s, new String[] { target }, ssl, port, use_v6).start();
					}
					
					new scheckEndTimer(delay + 1, source, target);
					
					moo.reply(source, target, "[SCHECK] Queued " + delay + " checks in the next " + (delay * scheckTimer.delay) + " seconds");
				}
			}
		}
	}
}
