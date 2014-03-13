package net.rizon.moo.plugin.dnsbl;

import java.util.Iterator;

import net.rizon.moo.Command;
import net.rizon.moo.Message;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;
import net.rizon.moo.Timer;

public class dnsbl extends Plugin
{
	private Command dnsbl;
	private Timer requester;
	private Message n219;
	
	public dnsbl()
	{
		super("DNSBL", "Monitors and shows DNSBL hits");
	}

	@Override
	public void start() throws Exception
	{
		dnsbl = new CommandDnsbl(this);
		requester = new StatsRequester();
		n219 = new Numeric219();
	}

	@Override
	public void stop()
	{
		dnsbl.remove();
		requester.stop();
		n219.remove();
	}
	
	
	public static long getDnsblFor(Server s)
	{
		long i = 0;
		for (Iterator<String> it = s.dnsbl.keySet().iterator(); it.hasNext();)
			i += s.dnsbl.get(it.next());
		return i;
	}
}