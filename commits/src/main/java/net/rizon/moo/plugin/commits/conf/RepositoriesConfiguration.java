package net.rizon.moo.plugin.commits.conf;

import java.util.List;
import java.util.ListIterator;
import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

/**
 *
 * @author Orillion {@literal <orillion@rizon.net>}
 */
public class RepositoriesConfiguration extends Configuration
{
	public List<String> repositories;
	public List<String> channels;

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateNotNull("Repositories", repositories);
		Validator.validateChannelList("Respository Channels", channels);

		ListIterator<String> it = repositories.listIterator();

		while (it.hasNext())
		{
			it.set(it.next().toLowerCase());
		}

		// In case we get java 8
//		repositories = repositories.stream()
//				.map(String::toLowerCase)
//				.collect(Collectors.toList());
	}
}
