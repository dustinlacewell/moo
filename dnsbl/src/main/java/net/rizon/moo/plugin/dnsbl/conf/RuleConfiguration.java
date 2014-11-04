package net.rizon.moo.plugin.dnsbl.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class RuleConfiguration extends Configuration
{
	public String reply, action;

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateNotEmpty("DNSBL Rule Host", reply);
		Validator.validateNotEmpty("DNSBL Rule action", action);
	}

	@Override
	public String toString()
	{
		return reply + ":" + action;
	}
}
