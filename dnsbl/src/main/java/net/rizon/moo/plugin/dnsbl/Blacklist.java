package net.rizon.moo.plugin.dnsbl;

import java.util.ArrayList;
import java.util.List;

import net.rizon.moo.plugin.dnsbl.actions.Action;

public class Blacklist
{
	private String name;
	private List<Rule> rules = new ArrayList<Rule>();

	public Blacklist(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	public void addRule(Rule r)
	{
		this.rules.add(r);
	}

	public void removeRule(Rule r)
	{
		this.rules.remove(r);
	}

	public List<Rule> getRules()
	{
		return this.rules;
	}

	public boolean hasRule(String response, Action action)
	{
		return getRule(response, action) != null;
	}

	public Rule getRule(String response, Action action)
	{
		Rule needle = new Rule(this, response, action);
		for (Rule r : rules)
			if (r.equals(needle))
				return r;

		return null;
	}
}
