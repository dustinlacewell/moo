package net.rizon.moo.protocol;

import net.rizon.moo.event;
import net.rizon.moo.message;

public class messageWallops extends message
{
	public messageWallops()
	{
		super("WALLOPS");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 1)
			return;
		
		for (event e : event.getEvents())
			e.onWallops(source, message[0]);
	}
}