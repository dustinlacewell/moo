package net.rizon.moo.protocol;

import net.rizon.moo.Message;
import net.rizon.moo.Server;

/* LINKS */
public class Message364 extends Message
{
	public Message364()
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
		
		Server s = Server.findServerAbsolute(from);
		if (s == null)
			s = new Server(from);
		s.link(to);
		
		s = Server.findServerAbsolute(to);
		if (s == null)
			s = new Server(to);
		s.link(from);
	}
}