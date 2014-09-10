package net.rizon.moo.plugin.dnsbl;

import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

public class dnsbl extends Plugin
{
	protected static final Logger log = Logger.getLogger(dnsbl.class.getName());

	private CommandDnsbl command;
	private BlacklistManager blacklistManager;
	private ResultCache cache;
	private EventDnsblCheck event;

	public dnsbl()
	{
		super("DNSBL", "Monitors connections for DNSBL hits and takes action.");
	}

	@Override
	public void start()
	{
		this.blacklistManager = new BlacklistManager();
		this.cache = new ResultCache();
		this.command = new CommandDnsbl(this, this.blacklistManager, this.cache);
		this.event = new EventDnsblCheck(this.blacklistManager, this.cache);

		DnsblChecker.loadSettingsFromConfiguration(Moo.conf);
		this.cache.loadSettingsFromConfiguration(Moo.conf);
		this.blacklistManager.loadRulesFromConfiguration(Moo.conf);
	}

	@Override
	public void stop()
	{
		this.command.remove();
		this.event.remove();
	}
	
	static DnsblInfo getDnsblInfoFor(Server s)
	{
		DnsblInfo i = infos.get(s);
		if (i == null)
		{
			i = new DnsblInfo();
			infos.put(s, i);
		}
		return i;
	}
}
