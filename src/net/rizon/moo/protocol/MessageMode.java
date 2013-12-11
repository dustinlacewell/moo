package net.rizon.moo.protocol;

import net.rizon.moo.Event;
import net.rizon.moo.Message;

public class MessageMode extends Message
{
	public MessageMode()
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
		
		for (Event e : Event.getEvents())
			e.onMode(source, message[0], modes);
	}
}
