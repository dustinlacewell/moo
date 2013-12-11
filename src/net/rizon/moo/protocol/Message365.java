package net.rizon.moo.protocol;

import net.rizon.moo.Event;
import net.rizon.moo.Message;

/* End of LINKS */
public class Message365 extends Message
{
	public Message365()
	{
		super("365");
	}

	@Override
	public void run(String source, String[] message)
	{
		for (Event e : Event.getEvents())
			e.loadDatabases();
	}
}
