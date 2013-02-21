package net.rizon.moo.protocol;

import net.rizon.moo.event;
import net.rizon.moo.message;

public class messageMode extends message
{
	public messageMode()
	{
		super("MODE");
	}
	
	@Override
	public void run(String source, String[] message)
	{
		String modes = "";
		for (int i = 1; i < message.length; ++i)
			modes += message[i] + " ";
		modes = modes.trim();
		
		for (event e : event.getEvents())
			e.onMode(source, message[0], modes);
	}
}
