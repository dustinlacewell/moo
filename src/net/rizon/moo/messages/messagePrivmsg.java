package net.rizon.moo.messages;

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
			moo.sock.privmsg(source, "\1VERSION " + moo.conf.getVersion() + "\1");
	}
}
