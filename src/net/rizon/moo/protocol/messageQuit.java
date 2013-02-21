package net.rizon.moo.protocol;

import net.rizon.moo.event;
import net.rizon.moo.message;


public class messageQuit extends message
{
	public messageQuit()
	{
		super("QUIT");
	}
	
	@Override
	public void run(String source, String[] message)
	{
		for (event e : event.getEvents())
			e.onQuit(source, message[0]);
	}
}