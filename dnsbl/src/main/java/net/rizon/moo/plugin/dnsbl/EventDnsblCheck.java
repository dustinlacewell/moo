package net.rizon.moo.plugin.dnsbl;

import net.rizon.moo.Config;
import net.rizon.moo.Event;
import net.rizon.moo.plugin.dnsbl.actions.Action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class EventDnsblCheck extends Event
{
	private BlacklistManager rules;
	private ResultCache cache;


	public EventDnsblCheck(BlacklistManager rules, ResultCache cache)
	{
		this.rules = rules;
		this.cache = cache;
	}

	private void takeAction(String ip, List<DnsblCheckResult> results)
	{
		if (results.isEmpty())
			return;

		// Unique actions can only be executed once.
		Set<String> takenActions = new HashSet<String>();

		// Iterate over results and execute all the actions.
		// We have multiple DNSBL server results for an IP...
		for (DnsblCheckResult result : results)
			// Each result contains multiple DNS responses... (e.g. multiple infractions or categories)
			for (Map.Entry<String, List<Action>> dnsblResult : result.getActions().entrySet())
				// And those responses each have a set of actions.
				for (Action a : dnsblResult.getValue())
					// Don't perform unique action types multiple times.
					if (!a.isUnique() || !takenActions.contains(a.getName()))
					{
						takenActions.add(a.getName());
						a.onHit(result.getBlacklist(), dnsblResult.getKey(), ip);
					}
	}

	@Override
	public void onClientConnect(final String nick, final String ident, final String ip, final String realname)
	{
		ResultCache.Entry entry = this.cache.hasEntry(ip);
		if (entry != null)
		{
			// Use cached entry to take action.
			this.takeAction(ip, entry.getResults());
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
			public void onResult(DnsblCheckResult result)
			{
				results.add(result);
			}

			public void onDone()
			{
				cache.addEntry(ip, results);
				takeAction(ip, results);
			}
		});
		checker.runAsynchronous();
	}

	@Override
	public void onReload(Config c)
	{
		rules.loadRulesFromConfiguration(c);
		cache.loadSettingsFromConfiguration(c);
		cache.clear();
		DnsblChecker.loadSettingsFromConfiguration(c);
	}
}