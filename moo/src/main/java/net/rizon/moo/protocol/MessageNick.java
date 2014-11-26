package net.rizon.moo.protocol;

import net.rizon.moo.Event;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;

public class MessageNick extends Message
{
	public MessageNick()
	{
		super("NICK");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length != 1)
			return;

		for (Event e : Event.getEvents())
			e.onNick(source, message[0]);

		Moo.users.renameUser(Moo.users.find(source), message[0]);
	}
}