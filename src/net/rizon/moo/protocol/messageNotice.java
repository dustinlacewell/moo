package net.rizon.moo.protocol;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.event;
import net.rizon.moo.message;

public class messageNotice extends message
{
	private static final Pattern connectPattern = Pattern.compile(".* Client connecting.*: ([^ ]*) \\(~?([^@]*).*?\\) \\[([0-9.]*)\\] \\[(.*)\\]");

	public messageNotice()
	{
		super("NOTICE");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 2)
			return;
		
		for (event e : event.getEvents())
			e.onNotice(source, message[0], message[1]);
		
		Matcher m = connectPattern.matcher(message[1]);
		if (m.matches())
		{
			if (source.indexOf('.') == -1)
				return;
			
			final String nick = m.group(1), ident = m.group(2), ip = m.group(3), realname = m.group(4);
			for (event e : event.getEvents())
				e.onClientConnect(nick, ident, ip, realname);
		}
	}
}