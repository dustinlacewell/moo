package net.rizon.moo.protocol;

import net.rizon.moo.event;
import net.rizon.moo.message;

/* End of LINKS */
public class message365 extends message
{
	public message365()
	{
		super("365");
	}

	@Override
	public void run(String source, String[] message)
	{
		for (event e : event.getEvents())
			e.loadDatabases();
	}
}
