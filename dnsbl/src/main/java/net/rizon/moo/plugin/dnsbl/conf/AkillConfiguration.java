package net.rizon.moo.plugin.dnsbl.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class AkillConfiguration extends Configuration
{
	public String duration, message;

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateNotEmpty("Akill duration", duration);
		Validator.validateNotEmpty("Akill message", message);
	}
}
