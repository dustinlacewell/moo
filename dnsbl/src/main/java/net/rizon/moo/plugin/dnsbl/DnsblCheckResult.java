package net.rizon.moo.plugin.dnsbl;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import net.rizon.moo.plugin.dnsbl.actions.Action;

class DnsblCheckResult
{
	private InetAddress ip;
	private Blacklist blacklist;
	private Map<String, List<Action>> actions;

	public DnsblCheckResult(InetAddress ip, Blacklist blacklist, Map<String, List<Action>> actions)
	{
		this.ip = ip;
		this.blacklist = blacklist;
		this.actions = actions;
	}

	public InetAddress getIP()
	{
		return this.ip;
	}

	public Blacklist getBlacklist()
	{
		return this.blacklist;
	}

	public Map<String, List<Action>> getActions()
	{
		return this.actions;
	}
}
