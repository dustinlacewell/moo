package net.rizon.moo.plugin.dnsbl.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class CacheConfiguration extends Configuration
{
	public int lifetime;

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validatePositive("DNSBL Cache lifetime", lifetime);
	}
}
