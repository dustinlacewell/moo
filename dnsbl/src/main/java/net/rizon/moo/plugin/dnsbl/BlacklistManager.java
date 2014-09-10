package net.rizon.moo.plugin.dnsbl;

import net.rizon.moo.Config;
import net.rizon.moo.plugin.dnsbl.actions.Action;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;


class BlacklistManager
{
	private Map<String, Blacklist> blacklists = new HashMap<String, Blacklist>();

	public void loadRulesFromConfiguration(Config c)
	{
		String[] hosts = c.getList("dnsbl.servers");

		blacklists.clear();

		for (String host : hosts)
		{
			if (host.isEmpty())
				// Config artifact: if dnsbl.servers is not set, conf.getList() will return new String[] { "" }.
				continue;

			Blacklist b = new Blacklist(host);
			blacklists.put(host, b);

			String[] rules = c.getList("dnsbl.rules." + host);
			for (String rawRule : rules)
			{
				// Rule format: RESPONSE + ":" + ACTION-NAME
				String[] ruleParts = rawRule.split(":");
				if (ruleParts.length != 2)
				{
					// Invalid config item, ignore it.
					dnsbl.log.log(Level.WARNING, "Invalid rule format: " + rawRule);
					continue;
				}

				String response = ruleParts[0];
				if (response.equals(Rule.RESPONSE_ANY))
					response = null;

				Action action = Action.getByName(ruleParts[1]);
				if (action == null)
				{
					// Invalid config item, ignore it.
					dnsbl.log.log(Level.WARNING, "Invalid action '" + ruleParts[1] + "' in rule: " + rawRule);
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
