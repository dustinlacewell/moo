package net.rizon.moo.plugin.grapher.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class GrapherConfiguration extends Configuration
{
	public String bin, dir;

	public static GrapherConfiguration load() throws Exception
	{
		return GrapherConfiguration.load("grapher.yml", GrapherConfiguration.class);
	}

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validatePath("Grapher bin", bin);
		Validator.validatePath("Grapher dir", dir);
	}
}
