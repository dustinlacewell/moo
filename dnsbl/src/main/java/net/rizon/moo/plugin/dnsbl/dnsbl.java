package net.rizon.moo.plugin.dnsbl;

import net.rizon.moo.Logger;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.dnsbl.conf.DnsblConfiguration;

public class dnsbl extends Plugin
{
	protected static final Logger log = Logger.getLogger(dnsbl.class.getName());

	private CommandDnsbl command;
	private BlacklistManager blacklistManager;
	private ResultCache cache;
	private EventDnsblCheck event;
	public static DnsblConfiguration conf;

	public dnsbl() throws Exception
	{
		super("DNSBL", "Monitors connections for DNSBL hits and takes action.");
		conf = DnsblConfiguration.load();
	}

	@Override
	public void start()
	{
		this.blacklistManager = new BlacklistManager();
		this.cache = new ResultCache();
		this.command = new CommandDnsbl(this, this.blacklistManager, this.cache);
		this.event = new EventDnsblCheck(this.blacklistManager, this.cache);

		DnsblChecker.load(conf);
		this.cache.load(conf.cache);
		this.blacklistManager.load(conf.servers);
	}

	@Override
	public void stop()
	{
		this.command.remove();
		this.event.remove();
	}
}
