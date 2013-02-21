package net.rizon.moo.protocol;

import net.rizon.moo.event;
import net.rizon.moo.message;

public class messageJoin extends message
{
	public messageJoin()
	{
		super("JOIN");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length != 1)
			return;
		
		for (event e : event.getEvents())
			e.onJoin(source, message[0]);
	}
}