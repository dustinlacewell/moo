package net.rizon.moo.protocol;

import net.rizon.moo.message;
import net.rizon.moo.server;

/* /stats c */
public class message213 extends message
{
	public message213()
	{
		super("213");
	}

	@Override
	public void run(String source, String[] message)
	{
		server serv = server.findServerAbsolute(source);
		if (serv == null)
			serv = new server(source);
		
		if (message.length > 4)
			serv.clines_work.add(message[4]);
	}
}