package net.rizon.moo.plugin.dnsbl;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.rizon.moo.events.EventClientConnect;
import net.rizon.moo.events.EventDNSBLHit;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.plugin.dnsbl.actions.Action;
import org.slf4j.Logger;

class EventDnsblCheck implements EventListener
{
	@Inject
	private static Logger logger;

	@Inject
	private BlacklistManager rules;
	@Inject
	private ResultCache cache;

	@Inject
	private EventBus eventBus;

	private void takeAction(String nick, String ip, List<DnsblCheckResult> results)
	{
		if (results.isEmpty())
			return;

		// Unique actions can only be executed once.
		Set<String> takenActions = new HashSet<>();

		// Iterate over results and execute all the actions.
		// We have multiple DNSBL server results for an IP...
		for (DnsblCheckResult result : results)
			// Each result contains multiple DNS responses... (e.g. multiple infractions or categories)
			for (Map.Entry<String, List<Action>> dnsblResult : result.getActions().entrySet())
			{
				// XXX this is calling event from a thread
				eventBus.post(new EventDNSBLHit(nick, ip, result.getBlacklist().getName(), dnsblResult.getKey()));

				// And those responses each have a set of actions.
				for (Action a : dnsblResult.getValue())
					// Don't perform unique action types multiple times.
					if (!a.isUnique() || !takenActions.contains(a.getName()))
					{
						takenActions.add(a.getName());
						a.onHit(result.getBlacklist(), dnsblResult.getKey(), nick, ip);
					}
			}
	}

	@Subscribe
	public void onClientConnect(EventClientConnect evt)
	{
		final String ip = evt.getIp(), nick = evt.getNick();
		
		ResultCache.Entry entry = this.cache.hasEntry(ip);
		if (entry != null)
		{
			// Use cached entry to take action.
			this.takeAction(nick, ip, entry.getResults());
			return;
		}

		// Find target and determine if we should check it.
		final DnsblCheckTarget target = DnsblCheckTarget.find(ip);
		if (!target.shouldCheck())
			return;

		// Setup checker and results.
		final DnsblChecker checker = new DnsblChecker(target, this.rules);
		final List<DnsblCheckResult> results = new ArrayList<DnsblCheckResult>();

		// Run checker asynchronously.
		checker.addCallback(new DnsblCallback()
		{
			@Override
			public void onResult(DnsblCheckResult result)
			{
				results.add(result);
			}

			@Override
			public void onDone()
			{
				cache.addEntry(ip, results);
				takeAction(nick, ip, results);
			}
		});
		checker.runAsynchronous();
	}
}
