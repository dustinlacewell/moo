package net.rizon.moo.protocol;

import net.rizon.moo.Event;
import net.rizon.moo.Message;

public class MessageKick extends Message
{
	public MessageKick()
	{
		super("KICK");
	}
	
	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 2)
			return;
		
		for (Event e : Event.getEvents())
			e.onKick(source, message[1], message[0]);
	}
}