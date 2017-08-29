package net.rizon.moo.plugin.commands.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.plugin.commands.why.conf.WhyConfiguration;

public class CommandsConfiguration extends Configuration
{
	public WhyConfiguration why;

	public static CommandsConfiguration load() throws Exception
	{
		return Configuration.load("commands.yml", CommandsConfiguration.class);
	}

	@Override
	public void validate() throws ConfigurationException
	{
		why.validate();
	}
}
