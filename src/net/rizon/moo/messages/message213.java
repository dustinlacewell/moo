package net.rizon.moo.messages;

import net.rizon.moo.message;
import net.rizon.moo.server;

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
			serv.clines.add(message[4]);
	}
}