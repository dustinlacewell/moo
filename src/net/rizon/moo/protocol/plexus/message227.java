package net.rizon.moo.protocol.plexus;

import net.rizon.moo.message;
import net.rizon.moo.server;

/* /stats b */
class message227 extends message
{
	public message227()
	{
		super("227");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 4)
			return;
		
		final String name = message[2];
		long count;
		
		try
		{
			count = Long.parseLong(message[3]);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return;
		}
		
		server s = server.findServer(source);
		if (s == null)
			s = new server(source);

		s.dnsbl.put(name, count);
	}
}