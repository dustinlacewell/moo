package net.rizon.moo.plugin.osflood.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class OsfloodConfiguration extends Configuration
{
	public int time, num;

	public static OsfloodConfiguration load() throws Exception
	{
		return load("osflood.yml", OsfloodConfiguration.class);
	}

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateNotZero("OSFlood time", time);
		Validator.validatePositive("OSFlood time", time);
		Validator.validatePositive("OSFlood num", num);
	}
}
