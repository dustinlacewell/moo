package net.rizon.moo.plugin.dnsbl;

import com.google.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.rizon.moo.plugin.dnsbl.actions.Action;
import net.rizon.moo.plugin.dnsbl.conf.DnsblConfiguration;
import net.rizon.moo.plugin.dnsbl.conf.DnsblServerConfiguration;
import net.rizon.moo.plugin.dnsbl.conf.RuleConfiguration;
import org.slf4j.Logger;


class BlacklistManager
{
	@Inject
	private static Logger logger;

	@Inject
	private dnsbl dnsbl;

	@Inject
	private DnsblConfiguration conf;
	
	private Map<String, Blacklist> blacklists = new HashMap<>();

	public void load()
	{
		blacklists.clear();

		for (DnsblServerConfiguration c : conf.servers)
		{
			Blacklist b = new Blacklist(c.address);
			blacklists.put(c.address, b);

			for (RuleConfiguration rule : c.rules)
			{
				String response = rule.reply;
				if (response.equals(Rule.RESPONSE_ANY))
					response = null;

				Action action = dnsbl.getByName(rule.action);
				if (action == null)
				{
					// Invalid config item, ignore it.
					logger.warn("Invalid action ''{}'' in rule: {}", rule.action, rule.toString());
					continue;
				}

				Rule r = new Rule(b, response, action);
				b.addRule(r);
			}
		}
	}

	public void addBlacklist(Blacklist blacklist)
	{
		blacklists.put(blacklist.getName(), blacklist);
	}

	public Blacklist getBlacklist(String host)
	{
		return blacklists.get(host);
	}

	public void removeBlacklist(Blacklist blacklist)
	{
		blacklists.remove(blacklist.getName());
	}

	public Collection<Blacklist> getBlacklists()
	{
		return blacklists.values();
	}
}
