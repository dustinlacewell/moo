package net.rizon.moo.protocol;

import net.rizon.moo.Event;
import net.rizon.moo.Message;

public class MessageJoin extends Message
{
	public MessageJoin()
	{
		super("JOIN");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length != 1)
			return;
		
		for (Event e : Event.getEvents())
			e.onJoin(source, message[0]);
	}
}