package net.rizon.moo.plugin.dnsblstats;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.events.OnConnect;
import net.rizon.moo.events.OnServerDestroy;
import net.rizon.moo.events.OnServerLink;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;
import net.rizon.moo.plugin.dnsblstats.comparators.CountComparator;
import net.rizon.moo.plugin.dnsblstats.comparators.ServerComparator;

public class dnsblstats extends Plugin implements EventListener
{
	@Inject
	private CommandDnsblStats dnsbl;

	@Inject
	private ServerManager serverManager;

	@Inject
	private Protocol protocol;

	@Inject
	private StatsRequester requester;
	
	private ScheduledFuture requesterFuture;

	private Map<Server, DnsblInfo> infos = new HashMap<>();

	public dnsblstats()
	{
		super("DNSBL Stats", "Monitors and shows DNSBL hits");
	}

	@Override
	public void start() throws Exception
	{
		requesterFuture = Moo.scheduleWithFixedDelay(requester, 1, TimeUnit.MINUTES);
	}

	@Override
	public void stop()
	{
		requesterFuture.cancel(false);
	}

	public DnsblInfo getDnsblInfoFor(Server s)
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
		for (Server s : serverManager.getServers())
			protocol.write("STATS", "B", s.getName());
	}

	@Subscribe
	public void onServerLink(OnServerLink evt)
	{
		Server serv = evt.getServer();
		
		/* Be sure dnsbl stats are up to date, prevents long splits from tripping the dnsbl monitor */
		protocol.write("STATS", "B", serv.getName());
	}

	@Subscribe
	public void onServerDestroy(OnServerDestroy evt)
	{
		Server serv = evt.getServer();
		
		infos.remove(serv);
	}

	@Override
	public List<Command> getCommands()
	{
		return Arrays.<Command>asList(dnsbl);
	}

	@Override
	protected void configure()
	{
		bind(dnsblstats.class).toInstance(this);

		bind(StatsRequester.class);

		bind(Numeric219.class);
		bind(Numeric227.class);

		bind(CountComparator.class);
		bind(ServerComparator.class);

		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder(), Command.class);
		commandBinder.addBinding().to(CommandDnsblStats.class);

		Multibinder<EventListener> eventListenerBinder = Multibinder.newSetBinder(binder(), EventListener.class);
		eventListenerBinder.addBinding().toInstance(this);
	}
}
