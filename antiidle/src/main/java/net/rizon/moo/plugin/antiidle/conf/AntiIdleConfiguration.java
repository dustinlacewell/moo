package net.rizon.moo.plugin.antiidle.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class AntiIdleConfiguration extends Configuration
{
	public String channel;
	public int time, bantime;

	/**
	 * Loads Antiidle Configuration settings.
	 * @return Configuration settings.
	 * @throws Exception Thrown when something goes wrong.
	 */
	public static AntiIdleConfiguration load() throws Exception
	{
		return load("antiidle.yml", AntiIdleConfiguration.class);
	}

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateChannelName("AntiIdle channel", channel);
		Validator.validatePositive("AntiIdle time", time);
		Validator.validateNotZero("AntiIdle time", time);
		Validator.validatePositive("AntiIdle bantime", bantime);
	}
}
