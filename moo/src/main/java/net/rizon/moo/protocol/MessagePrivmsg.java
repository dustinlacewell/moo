package net.rizon.moo.protocol;

import java.util.Date;

import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.events.EventPrivmsg;

public class MessagePrivmsg extends Message
{
	public MessagePrivmsg()
	{
		super("PRIVMSG");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 2)
			return;

		Moo.getEventBus().post(new EventPrivmsg(source, message[0], message[1]));

		if (message[1].equals("\1VERSION\1"))
			Moo.notice(source, "\1VERSION " + Moo.conf.version + "\1");
		else if (message[1].equals("\1TIME\1"))
			Moo.notice(source, "\1TIME " + (new Date().toString()) + "\1");
	}
}