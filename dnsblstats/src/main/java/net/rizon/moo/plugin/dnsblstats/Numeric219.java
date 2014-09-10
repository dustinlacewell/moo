package net.rizon.moo.plugin.dnsblstats;

import net.rizon.moo.Message;

// End of stats
class Numeric219 extends Message
{
	Numeric219()
	{
		super("219");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message[1].equals("B") == false)
			return;
		
		CommandDnsblStats.checkReply(source);
		StatsRequester.checkWarn(source);
	}
}