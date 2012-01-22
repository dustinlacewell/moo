package net.rizon.moo.commands;

import java.util.Iterator;

import net.rizon.moo.command;
import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;

class message211 extends message
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
		server serv = server.findServerAbsolute(source);
		if (serv == null)
			serv = new server(source);
		serv.bytes += bytes;
	}
}

class message219 extends message
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
	
	
	public static String request_source = null;
	public static String request_target = null;
	public static boolean request_all = false;

	@Override
	public void run(String source, String[] message)
	{
		server serv = server.findServerAbsolute(source);
		if (serv == null || request_source == null || request_target == null || message[1].equals("?") == false)
			return;
		else if (request_all || serv.bytes >= 1024)
			moo.sock.reply(request_source, request_target, "[MAP] " + source + " " + this.convertBytes(serv.bytes));
	}
}

class message265 extends message
{
	public message265()
	{
		super("265");
	}
	
	public static String request_source = null;
	public static String request_target = null;
	public static int request_users = 0;

	@Override
	public void run(String source, String[] message)
	{
		if (request_source == null || request_target == null || message.length < 2)
			return;

		int users = Integer.parseInt(message[1]);
		if (users >= request_users)
			moo.sock.reply(request_source, request_target, "[MAP] " + source + " " + users);
	}
}

class commandMapBase extends command
{
	private boolean full;

	public commandMapBase(mpackage pkg, final String cmd, boolean full)
	{
		super(pkg, cmd, "View hub lag and routing information");
		this.full = full;
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length == 1)
		{
			for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
			{
				server s = it.next();
				if (s.isHub())
				{
					s.bytes = 0;
					moo.sock.write("STATS ? " + s.getName());
				}
			}
			message219.request_all = this.full;
			message219.request_source = source;
			message219.request_target = target;
		}
		else if (params.length > 1)
		{
			if (params[1].equalsIgnoreCase("HUB") && params.length > 2)
			{
				server s = server.findServer(params[2]);
				if (s == null)
					moo.sock.reply(source, target, "[MAP] Server " + params[2] + " not found");
				else
					for (Iterator<String> it = s.links.iterator(); it.hasNext();)
						moo.sock.reply(source, target, "[MAP] " + s.getName() + " is linked to " + it.next()); 
			}
			else
			{
				try
				{
					int users = Integer.parseInt(params[1]);

					for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
					{
						server s = it.next();
						moo.sock.write("USERS " + s.getName());
					}
					
					message265.request_source = source;
					message265.request_target = target;
					message265.request_users = users;
				}
				catch (NumberFormatException ex)
				{
					server s = server.findServer(params[1]);
					if (s == null)
						moo.sock.reply(source, target, "[MAP] Server " + params[1] + " not found");
					else
					{
						s.bytes = 0;
						moo.sock.write("STATS ? " + s.getName());;
						message219.request_all = this.full;
						message219.request_source = source;
						message219.request_target = target;
					}
				}
			}
		}
	}
}

class commandMapRegular extends commandMapBase
{
	public commandMapRegular(mpackage pkg)
	{
		super(pkg, "!MAP", false);
	}
}

class commandMapAll extends commandMapBase
{
	public commandMapAll(mpackage pkg)
	{
		super(pkg, "!MAP-" , true);
	}
}

public class commandMap
{
	@SuppressWarnings("unused")
	private static message211 msg_211 = new message211();
	@SuppressWarnings("unused")
	private static message219 msg_219 = new message219();
	@SuppressWarnings("unused")
	private static message265 msg_265 = new message265();
	@SuppressWarnings("unused")
	private commandMapRegular map_reg;
	@SuppressWarnings("unused")
	private commandMapAll map_all;
	
	public commandMap(mpackage pkg)
	{
		this.map_reg = new commandMapRegular(pkg);
		this.map_all = new commandMapAll(pkg);
	}
}
