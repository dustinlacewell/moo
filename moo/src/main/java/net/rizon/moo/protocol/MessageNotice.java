package net.rizon.moo.protocol;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.Event;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.events.EventClientConnect;
import net.rizon.moo.events.EventNotice;

public class MessageNotice extends Message
{
	private static final Pattern connectPattern = Pattern.compile(".* Client connecting.*: ([^ ]*) \\(~?([^@]*).*?\\) \\[([AaBbCcDdEeFf0-9.:]*)\\] (?:\\{[^}]*\\} )?\\[(.*)\\]");

	public MessageNotice()
	{
		super("NOTICE");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 2)
			return;

		Moo.getEventBus().post(new EventNotice(source, message[0], message[1]));

		Matcher m = connectPattern.matcher(message[1]);
		if (m.matches())
		{
			if (source.indexOf('@') != -1)
				return;

			final String nick = m.group(1), ident = m.group(2), ip = m.group(3), realname = m.group(4);
			Moo.getEventBus().post(new EventClientConnect(nick, ident, ip, realname));
		}
	}
}
