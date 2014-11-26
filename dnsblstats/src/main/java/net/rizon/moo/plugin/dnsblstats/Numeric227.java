package net.rizon.moo.plugin.dnsblstats;

import net.rizon.moo.Logger;
import net.rizon.moo.Message;
import net.rizon.moo.Server;

/* /stats b */
class Numeric227 extends Message
{
	public Numeric227()
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
			Logger.getGlobalLogger().log(ex);
			return;
		}

		Server s = Server.findServer(source);
		if (s == null)
			s = new Server(source);

		DnsblInfo info = dnsblstats.getDnsblInfoFor(s);
		info.hits.put(name, count);
	}
}
