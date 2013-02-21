package net.rizon.moo.protocol;

import net.rizon.moo.event;
import net.rizon.moo.message;

public class messageKick extends message
{
	public messageKick()
	{
		super("KICK");
	}
	
	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 2)
			return;
		
		for (event e : event.getEvents())
			e.onKick(source, message[1], message[0]);
	}
}