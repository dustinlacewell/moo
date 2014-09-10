package net.rizon.moo.plugin.dnsblstats;

import java.util.HashMap;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Message;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;
import net.rizon.moo.Timer;

public class dnsblstats extends Plugin
{
	private Command dnsbl;
	private Timer requester;
	private Message n219, n227;
	private Event e;
	
	static HashMap<Server, DnsblInfo> infos = new HashMap<Server, DnsblInfo>();
	
	public dnsblstats()
	{
		super("DNSBL Stats", "Monitors and shows DNSBL hits");
	}

	@Override
	public void start() throws Exception
	{
		dnsbl = new CommandDnsblStats(this);
		requester = new StatsRequester();
		n219 = new Numeric219();
		n227 = new Numeric227();
		e = new EventDnsbl();
		
		requester.start();
	}

	@Override
	public void stop()
	{
		dnsbl.remove();
		requester.stop();
		n219.remove();
		n227.remove();
		e.remove();
	}
	
	static DnsblInfo getDnsblInfoFor(Server s)
	{
		DnsblInfo i = infos.get(s);
		if (i == null)
		{
			i = new DnsblInfo();
			infos.put(s, i);
		}
		return i;
	}
}
