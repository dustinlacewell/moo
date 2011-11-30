package net.rizon.moo.messages;

import java.util.Date;

import net.rizon.moo.message;
import net.rizon.moo.moo;

public class messagePrivmsg extends message
{
	public messagePrivmsg()
	{
		super("PRIVMSG");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 2)
			return;

		if (message[1].equals("\1VERSION\1"))
			moo.sock.notice(source, "\1VERSION " + moo.conf.getVersion() + "\1");
		else if (message[1].equals("\1TIME\1"))
			moo.sock.notice(source, "\1TIME " + (new Date().toString()) + "\1");
		else if (message[1].startsWith("\1ACTION pets " + moo.conf.getNick()))
			moo.sock.privmsg(message[0], "\1ACTION moos\1");
		else if (message[1].startsWith("\1ACTION milks " + moo.conf.getNick()))
		{
			int e = source.indexOf('!');
			String nick = source.substring(0, e != -1 ? e : source.length());
			moo.sock.privmsg(message[0], "\1ACTION kicks " + nick + " in the face\1");
		}
	}
}
