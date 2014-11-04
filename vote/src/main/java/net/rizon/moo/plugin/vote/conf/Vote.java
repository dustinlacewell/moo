package net.rizon.moo.plugin.vote.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class Vote extends Configuration
{
	public String channel;
	public String email;

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateChannelName("Vote channel", channel);
		Validator.validateEmail("Vote Email", email);
	}
}
