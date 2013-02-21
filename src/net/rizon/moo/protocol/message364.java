package net.rizon.moo.protocol;

import net.rizon.moo.message;
import net.rizon.moo.server;

/* LINKS */
public class message364 extends message
{
	public message364()
	{
		super("364");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 3)
			return;

		String from = message[1];
		String to = message[2];
		
		if (from.equals(to))
			return;
		
		server s = server.findServerAbsolute(from);
		if (s == null)
			s = new server(from);
		s.link(to);
		
		s = server.findServerAbsolute(to);
		if (s == null)
			s = new server(to);
		s.link(from);
	}
}