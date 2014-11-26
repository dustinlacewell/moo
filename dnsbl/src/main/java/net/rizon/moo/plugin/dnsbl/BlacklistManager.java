package net.rizon.moo.plugin.dnsbl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.rizon.moo.plugin.dnsbl.actions.Action;
import net.rizon.moo.plugin.dnsbl.conf.DnsblServerConfiguration;
import net.rizon.moo.plugin.dnsbl.conf.RuleConfiguration;


class BlacklistManager
{
	private Map<String, Blacklist> blacklists = new HashMap<String, Blacklist>();

	/**
	 * Loads the list of DNSBL server configuration.
	 * @param configuration List of server configurations.
	 */
	public void load(List<DnsblServerConfiguration> configuration)
	{
		blacklists.clear();

		for (DnsblServerConfiguration c : configuration)
		{
			Blacklist b = new Blacklist(c.address);
			blacklists.put(c.address, b);

			for (RuleConfiguration rule : c.rules)
			{
				String response = rule.reply;
				if (response.equals(Rule.RESPONSE_ANY))
					response = null;

				Action action = Action.getByName(rule.action);
				if (action == null)
				{
					// Invalid config item, ignore it.
					dnsbl.log.log(Level.WARNING, "Invalid action ''{0}'' in rule: {1}", new Object[]{rule.action, rule.toString()});
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
