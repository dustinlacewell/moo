package net.rizon.moo.protocol;

import net.rizon.moo.Event;
import net.rizon.moo.Message;


public class MessageQuit extends Message
{
	public MessageQuit()
	{
		super("QUIT");
	}
	
	@Override
	public void run(String source, String[] message)
	{
		for (Event e : Event.getEvents())
			e.onQuit(source, message[0]);
	}
}