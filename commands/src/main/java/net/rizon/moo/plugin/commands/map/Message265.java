package net.rizon.moo.plugin.commands.map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;

public class Message265 extends Message
{
	public Message265()
	{
		super("265");
	}

	protected static CommandSource source;
	public static int request_users = 0;
	private static Pattern p = Pattern.compile("Current local users:? (\\d+)[ ,]*[Mm]ax:? (\\d+)");

	@Override
	public void run(IRCMessage message)
	{
		if (source == null || message.getParams().length < 2)
			return;

		Matcher m = p.matcher(message.getParams()[message.getParams().length - 1]);
		m.matches();
		int users = Integer.parseInt(m.group(1));

		if (users >= request_users)
			source.reply("[MAP] " + message.getSource() + " " + users);
	}
}