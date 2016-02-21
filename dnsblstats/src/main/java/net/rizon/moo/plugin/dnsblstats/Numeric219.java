package net.rizon.moo.plugin.dnsblstats;

import com.google.inject.Inject;
import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;

// End of stats
class Numeric219 extends Message
{
	@Inject
	private CommandDnsblStats stats;

	@Inject
	private StatsRequester requester;
	
	Numeric219()
	{
		super("219");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getParams()[1].equals("B") == false)
			return;

		stats.checkReply(message.getSource());
		requester.checkWarn(message.getSource());
	}
}
