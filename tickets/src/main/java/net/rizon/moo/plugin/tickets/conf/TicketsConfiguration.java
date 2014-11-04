package net.rizon.moo.plugin.tickets.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class TicketsConfiguration extends Configuration
{
	public String url, username, password;

	public static TicketsConfiguration load() throws Exception
	{
		return load("tickets.yml", TicketsConfiguration.class);
	}

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateURL("Tickets URL", url);
		Validator.validateNotEmpty("Tickets Username", username);
		// In case you're connecting to a site without a password. :D
		Validator.validateNullOrNotEmpty("Tickets Password", password);
	}
}
