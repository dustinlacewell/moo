package net.rizon.moo.protocol;

import net.rizon.moo.Event;
import net.rizon.moo.Message;

public class MessagePart extends Message
{
	public MessagePart()
	{
		super("PART");
	}
	
	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 1)
			return;
		
		for (Event e : Event.getEvents())
			e.onPart(source, message[0]);
	}
}
