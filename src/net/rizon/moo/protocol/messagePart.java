package net.rizon.moo.protocol;

import net.rizon.moo.event;
import net.rizon.moo.message;

public class messagePart extends message
{
	public messagePart()
	{
		super("PART");
	}
	
	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 1)
			return;
		
		for (event e : event.getEvents())
			e.onPart(source, message[0]);
	}
}
