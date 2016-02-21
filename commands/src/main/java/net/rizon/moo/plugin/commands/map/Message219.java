package net.rizon.moo.plugin.commands.map;

import com.google.inject.Inject;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;

public class Message219 extends Message
{
	@Inject
	private ServerManager serverManager;

	public Message219()
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
	public void run(IRCMessage message)
	{
		Server serv = serverManager.findServerAbsolute(message.getSource());

		if (serv == null || source == null || message.getParams()[1].equals("?") == false)
			return;

		if (request_all || serv.bytes >= 1024)
			source.reply("[MAP] " + message.getSource() + " " + this.convertBytes(serv.bytes));
	}
}
