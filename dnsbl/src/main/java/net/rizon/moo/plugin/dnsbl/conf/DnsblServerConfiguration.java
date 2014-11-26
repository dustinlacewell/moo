package net.rizon.moo.plugin.dnsbl.conf;

import java.util.List;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class DnsblServerConfiguration extends Configuration
{
	public String address;
	public List<RuleConfiguration> rules;

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateHost("DNSBL Server address", address);
		Validator.validateList(rules);
	}
}
