package net.rizon.moo.protocol;

import net.rizon.moo.event;
import net.rizon.moo.message;

public class messageNick extends message
{
	public messageNick()
	{
		super("NICK");
	}
	
	@Override
	public void run(String source, String[] message)
	{
		if (message.length != 1)
			return;
		
		for (event e : event.getEvents())
			e.onNick(source, message[0]);
	}
}