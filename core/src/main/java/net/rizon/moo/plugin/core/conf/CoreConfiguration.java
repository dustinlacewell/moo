package net.rizon.moo.plugin.core.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;

public class CoreConfiguration extends Configuration
{
	public ShellConfiguration shell;

	public static CoreConfiguration load() throws Exception
	{
		return CoreConfiguration.load("core.yml", CoreConfiguration.class);
	}

	@Override
	public void validate() throws ConfigurationException
	{
		shell.validate();
	}
}
