package net.rizon.moo.plugin.dnsblstats;

import com.google.inject.Inject;
import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;
import org.slf4j.Logger;

/* /stats b */
class Numeric227 extends Message
{
	@Inject
	private static Logger logger;

	@Inject
	private ServerManager serverManager;

	@Inject
	private dnsblstats dnsblstats;
		
	public Numeric227()
	{
		super("227");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getParams().length < 4)
			return;

		final String name = message.getParams()[2];
		long count;

		try
		{
			count = Long.parseLong(message.getParams()[3]);
		}
		catch (Exception ex)
		{
			logger.warn("Unable to parse 277", ex);
			return;
		}

		Server s = serverManager.findServer(message.getSource());
		if (s == null)
		{
			s = new Server(message.getSource());
			serverManager.insertServer(s);
		}

		DnsblInfo info = dnsblstats.getDnsblInfoFor(s);
		info.hits.put(name, count);
	}
}
