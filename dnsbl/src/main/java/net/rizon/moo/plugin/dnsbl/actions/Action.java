package net.rizon.moo.plugin.dnsbl.actions;

import net.rizon.moo.plugin.dnsbl.Blacklist;

public abstract class Action
{
	private String name;
	private String description;

	public Action(String name, String description)
	{
		this.name = name;
		this.description = description;
	}

	public abstract void onHit(Blacklist blacklist, String dnsblResponse, String nick, String ip);

	public boolean isUnique()
	{
		return false;
	}

	public String getName()
	{
		return this.name;
	}

	public String getDescription()
	{
		return this.description;
	}
}
