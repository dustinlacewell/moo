package net.rizon.moo.protocol;

import java.util.Date;

import net.rizon.moo.event;
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
		
		for (event e : event.getEvents())
			e.onPrivmsg(source, message[0], message[1]);

		if (message[1].equals("\1VERSION\1"))
			moo.notice(source, "\1VERSION " + moo.conf.getVersion() + "\1");
		else if (message[1].equals("\1TIME\1"))
			moo.notice(source, "\1TIME " + (new Date().toString()) + "\1");
	}
}