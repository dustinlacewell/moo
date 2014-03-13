package net.rizon.moo.plugin.dnsbl;

import java.util.HashMap;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Message;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;
import net.rizon.moo.Timer;

public class dnsbl extends Plugin
{
	private Command dnsbl;
	private Timer requester;
	private Message n219;
	private Event e;
	
	static HashMap<Server, DnsblInfo> infos = new HashMap<Server, DnsblInfo>();
	
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
		e = new EventDnsbl();
	}

	@Override
	public void stop()
	{
		dnsbl.remove();
		requester.stop();
		n219.remove();
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