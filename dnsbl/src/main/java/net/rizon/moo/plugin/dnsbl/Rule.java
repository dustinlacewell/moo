package net.rizon.moo.plugin.dnsbl;

import net.rizon.moo.plugin.dnsbl.actions.Action;

class Rule
{
	public static final String RESPONSE_ANY = "*";
	private Blacklist blacklist;
	private String response;
	private Action action;

	public Rule(Blacklist blacklist, String response, Action action)
	{
		this.blacklist = blacklist;
		this.response = response;
		this.action = action;
	}

	public Blacklist getBlacklist()
	{
		return this.blacklist;
	}

	public String getResponse()
	{
		return this.response;
	}

	public Action getAction()
	{
		return this.action;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Rule)
		{
			Rule other = (Rule) o;

			if (!this.action.equals(other.action))
				return false;

			return (this.response == null && other.response == null) ||
					(this.response != null && this.response.equals(other.response));

		}
		return false;
	}
}
