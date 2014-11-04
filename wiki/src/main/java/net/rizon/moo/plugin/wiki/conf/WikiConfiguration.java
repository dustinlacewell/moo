package net.rizon.moo.plugin.wiki.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class WikiConfiguration extends Configuration
{
	public String url;

	/**
	 * Loads Wiki Configuration settings.
	 * @return Configuration settings.
	 * @throws Exception Thrown when something goes wrong.
	 */
	public static WikiConfiguration load() throws Exception
	{
		return load("wiki.yml", WikiConfiguration.class);
	}

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateURL("Wiki URL", url);
	}
}
