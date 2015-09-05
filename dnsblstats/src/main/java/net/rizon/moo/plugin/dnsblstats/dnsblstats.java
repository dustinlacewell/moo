package net.rizon.moo.plugin.dnsblstats;

import com.google.common.eventbus.Subscribe;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;
import net.rizon.moo.events.OnConnect;
import net.rizon.moo.events.OnServerDestroy;
import net.rizon.moo.events.OnServerLink;

public class dnsblstats extends Plugin
{
	private Command dnsbl;
	private ScheduledFuture requester;
	private Message n219, n227;

	static HashMap<Server, DnsblInfo> infos = new HashMap<Server, DnsblInfo>();

	public dnsblstats()
	{
		super("DNSBL Stats", "Monitors and shows DNSBL hits");
	}

	@Override
	public void start() throws Exception
	{
		dnsbl = new CommandDnsblStats(this);
		requester = Moo.scheduleWithFixedDelay(new StatsRequester(), 1, TimeUnit.MINUTES);
		n219 = new Numeric219();
		n227 = new Numeric227();
		Moo.getEventBus().register(this);
	}

	@Override
	public void stop()
	{
		dnsbl.remove();
		requester.cancel(false);
		n219.remove();
		n227.remove();
		Moo.getEventBus().unregister(this);
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
	
	@Subscribe
	public void onConnect(OnConnect evt)
	{
		for (Server s : Server.getServers())
			Moo.write("STATS", "B", s.getName());
	}

	@Subscribe
	public void onServerLink(OnServerLink evt)
	{
		Server serv = evt.getServer();
		
		/* Be sure dnsbl stats are up to date, prevents long splits from tripping the dnsbl monitor */
		Moo.write("STATS", "B", serv.getName());
	}

	@Subscribe
	public void onServerDestroy(OnServerDestroy evt)
	{
		Server serv = evt.getServer();
		
		dnsblstats.infos.remove(serv);
	}
}
