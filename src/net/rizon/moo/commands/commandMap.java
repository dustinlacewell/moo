package net.rizon.moo.commands;

import java.util.Iterator;

import net.rizon.moo.command;
import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.server;

class message211 extends message
{
	public message211()
	{
		super("211");
	}
	
	public static String request_chan = null;
	public static boolean request_all = false;
	
	private String convertBytes(String b)
	{
		double bb = Double.parseDouble(b);
		String what = "bytes";
		
		if (bb > 1024D)
		{
			bb /= 1024D;
			what = "KB";
		}
		if (bb > 1024D)
		{
			bb /= 1024D;
			what = "MB";
		}
		if (bb > 1024D)
		{
			bb /= 1024D;
			what = "GB";
		}
		if (bb > 1024D)
		{
			bb /= 1024D;
			what = "TB";
		}
		
		b = Double.toString(bb);
		int dp = b.indexOf('.');
		if (b.length() > dp + 2)
			return b.substring(0, dp + 3) + " " + what;
		else
			return b + " " + what;
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
		if (request_chan == null)
			return;

		int lag = Integer.parseInt(message[2]);
		if (lag > 1024 || request_all)
		{
			String to_server = message[1].split("\\[")[0];
			
			moo.sock.privmsg(request_chan, "[MAP] " + source + " -> " + to_server + ": " + this.convertBytes(message[2]));
		}
	}
}

class commandMapBase extends command
{
	private boolean full;

	public commandMapBase(final String cmd, boolean full)
	{
		super(cmd);
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
				moo.sock.write("STATS ? " + s.getName());
			}
			message211.request_all = this.full;
			message211.request_chan = target;
		}
		else if (params.length > 1)
		{
			server s = server.findServer(params[1]);
			if (s == null)
				moo.sock.privmsg(target, "[MAP] Server " + params[1] + " not found");
			else
			{
				moo.sock.write("STATS ? " + s.getName());;
				message211.request_all = this.full;
				message211.request_chan = target;
			}
		}
	}
}

class commandMapRegular extends commandMapBase
{
	public commandMapRegular()
	{
		super("!MAP", false);
	}
}

class commandMapAll extends commandMapBase
{
	public commandMapAll()
	{
		super("!MAP-" , true);
	}
}

public class commandMap
{
	@SuppressWarnings("unused")
	private static message211 msg_211 = new message211();
	@SuppressWarnings("unused")
	private static commandMapRegular map_reg = new commandMapRegular();
	@SuppressWarnings("unused")
	private static commandMapAll map_all = new commandMapAll();
}
