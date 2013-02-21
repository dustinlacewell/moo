package net.rizon.moo.protocol;

import net.rizon.moo.event;
import net.rizon.moo.message;

public class messageNotice extends message
{
	public messageNotice()
	{
		super("NOTICE");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 2)
			return;
		
		for (event e : event.getEvents())
			e.onNotice(source, message[0], message[1]);
	}
}