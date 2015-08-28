package net.rizon.moo.plugin.dnsblstats;

import net.rizon.moo.Message;
import net.rizon.moo.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* /stats b */
class Numeric227 extends Message
{
	private static final Logger logger = LoggerFactory.getLogger(Numeric227.class);
		
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
			logger.warn("Unable to parse 277", ex);
			return;
		}

		Server s = Server.findServer(source);
		if (s == null)
			s = new Server(source);

		DnsblInfo info = dnsblstats.getDnsblInfoFor(s);
		info.hits.put(name, count);
	}
}
