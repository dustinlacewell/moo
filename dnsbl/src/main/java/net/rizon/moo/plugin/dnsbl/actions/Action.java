package net.rizon.moo.plugin.dnsbl.actions;

import net.rizon.moo.plugin.dnsbl.Blacklist;

public abstract class Action
{
	private static Action[] allActions = { new ActionAkill(), new ActionLog() };

	private String name;
	private String description;

	public Action(String name, String description)
	{
		this.name = name;
		this.description = description;
	}

	public abstract void onHit(Blacklist blacklist, String dnsblResponse, String user);

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

	public static Action[] getAllActions()
	{
		return allActions;
	}

	public static Action getByName(String name)
	{
		for (Action a : getAllActions())
			if (a.name.equals(name))
				return a;

		return null;
	}
}
