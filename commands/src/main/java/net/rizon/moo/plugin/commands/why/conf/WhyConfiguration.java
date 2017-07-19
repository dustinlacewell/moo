package net.rizon.moo.plugin.commands.why.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;

import java.util.List;

public class WhyConfiguration extends Configuration
{
	public List<String> servers;

	@Override
	public void validate() throws ConfigurationException
	{

	}
}
