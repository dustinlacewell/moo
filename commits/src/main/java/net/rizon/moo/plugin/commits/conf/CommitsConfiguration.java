package net.rizon.moo.plugin.commits.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class CommitsConfiguration extends Configuration
{
	public String ip;
	public int port;
	public String[] channels;

	/**
	 * Loads Commits Configuration settings.
	 * @return Configuration settings.
	 * @throws Exception Thrown when something goes wrong.
	 */
	public static CommitsConfiguration load() throws Exception
	{
		return load("commits.yml", CommitsConfiguration.class);
	}

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateHost("Commits ip", ip);
		Validator.validatePort("Commits port", port, false);
		Validator.validateChannelList("Commits channels", channels);
	}
}
