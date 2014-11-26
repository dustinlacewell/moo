package net.rizon.moo.plugin.dnsbl.conf;

import java.util.List;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class DnsblConfiguration extends Configuration
{
	public String resolver;
	public List<DnsblServerConfiguration> servers;
	public AkillConfiguration akill;
	public CacheConfiguration cache;

	/**
	 * Loads the configuration.
	 * @return Configuration settings.
	 * @throws Exception Thrown when
	 */
	public static DnsblConfiguration load() throws Exception
	{
		return load("dnsbl.yml", DnsblConfiguration.class);
	}

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateHost("DNSBL Resolver", resolver);
		Validator.validateList(servers);
		akill.validate();
		cache.validate();
	}
}
