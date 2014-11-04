package net.rizon.moo.plugin.core.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class ShellConfiguration extends Configuration
{
	public boolean enabled;
	public String base;

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateNotNull("Core Shell enabled", enabled);
		Validator.validatePath("Core Shell base", base);
	}
}
