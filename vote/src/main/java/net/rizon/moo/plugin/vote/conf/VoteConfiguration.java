package net.rizon.moo.plugin.vote.conf;

import java.util.List;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class VoteConfiguration extends Configuration
{
	public List<Vote> vote;

	public static VoteConfiguration load() throws Exception
	{
		return load("vote.yml", VoteConfiguration.class);
	}

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateList(vote);
	}
}
