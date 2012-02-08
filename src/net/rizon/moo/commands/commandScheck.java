package net.rizon.moo.commands;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.util.Random;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;
import net.rizon.moo.socket;

class scheck extends Thread
{
	private String server;
	private int port;
	private String source;
	private String target;
	private String users;
	private String servers;
	
	private static final Random rand = new Random();
	private static final String getRandom()
	{
		String buf = "";
		for (int i = 0; i < 5; ++i)
		{
			char c;
			do
			{
				int j = rand.nextInt(57);
				j += 65;
				c = (char) j;
			}
			while (Character.isLetter(c) == false);
			buf += c;
		}
		
		return buf;
	}

	public scheck(final String server, final String source, final String target, int port)
	{
		this.server = server;
		this.source = source;
		this.target = target;
		this.port = port;
	}
	
	@Override
	public void run()
	{
		try
		{
			socket s = socket.create();
			moo.reply(this.source, this.target, "[SCHECK] Connecting to " + this.server + "...");
			s.connect(this.server, this.port, 15000);

			s.write("USER " + moo.conf.getIdent() + " . . :" + moo.conf.getRealname());
			s.write("NICK " + moo.conf.getNick() + "-" + getRandom());
			
			for (String in; (in = s.read()) != null;)
			{
				String[] token = in.split(" ");
				
				if (token.length > 11 && token[1].equals("251"))
				{
					this.servers = token[11];
				}
				else if (token.length > 8 && token[1].equals("266"))
				{
					this.users = token[8].replace(",", "");
					s.write("STATS u");
				}
				else if (token.length > 7 && token[1].equals("242"))
				{
					moo.reply(this.source, this.target, "[SCHECK] [" + token[0].substring(1) + "] Global users: " + this.users + ", Servers: " + this.servers + ", Uptime: " + token[5] + " days " + token[7]);
					s.shutdown();
					break;
				}
			}
		}
		catch (NoRouteToHostException ex)
		{
			moo.reply(this.source, this.target, "[SCHECK] Unable to connect to " + this.server + ", no route to host");
		}
		catch (SocketTimeoutException ex)
		{
			moo.reply(this.source, this.target, "[SCHECK] Unable to connect to " + this.server + ", connection timeout");
		}
		catch (IOException ex)
		{
			moo.reply(this.source, this.target, "[SCHECK] Unable to connect to " + this.server);
		}
	}
}

public class commandScheck extends command
{
	public commandScheck(mpackage pkg)
	{
		super(pkg, "!SCHECK", "Check if a server is online");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length == 1)
			moo.reply(source, target, "Syntax: !scheck <server> [port]");
		else
		{
			server serv = server.findServer(params[1]);
			if (serv == null)
				moo.reply(source, target, "[SCHECK] Server " + params[1] + " not found");
			else
			{
				int port = 6667;
				if (params.length > 2)
				{
					try
					{
						port = Integer.parseInt(params[2]);
						if (port <= 0 || port > 65535)
							throw new NumberFormatException("Invalid port range");
					}
					catch (NumberFormatException ex)
					{
					}
				}
				scheck check = new scheck(serv.getName(), source, target, port);
				check.start();
			}
		}
	}
}
