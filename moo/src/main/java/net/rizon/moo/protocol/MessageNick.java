package net.rizon.moo.protocol;

import net.rizon.moo.Event;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.events.EventNickChange;

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

		Moo.getEventBus().post(new EventNickChange(source, message[0]));

		Moo.users.renameUser(Moo.users.find(source), message[0]);
	}
}