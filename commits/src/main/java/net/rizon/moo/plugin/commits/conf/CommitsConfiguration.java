package net.rizon.moo.plugin.commits.conf;

import java.util.ArrayList;
import java.util.List;
import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;


public class CommitsConfiguration extends Configuration
{
	public String ip;
	public int port;
	public List<RepositoriesConfiguration> channels;
	public List<String> defaultChannels;

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
		Validator.validateChannelList("Default commits channels", defaultChannels);

		Validator.validateList(channels);
	}

	/**
	 * Returns all channels that are specified for the given repository.
	 * <p>
	 * @param repository Repository to get channels for.
	 * <p>
	 * @return All defined channels, or the default channel(s).
	 */
	public List<String> getChannelsForRepository(String repository)
	{
		boolean found = false;

		List<String> chans = new ArrayList<>();

		for (RepositoriesConfiguration conf : channels)
		{
			if (conf.repositories.contains(repository.toLowerCase()))
			{
				chans.addAll(conf.channels);

				found = true;
			}
		}

		return found ? chans : defaultChannels;
	}
}
