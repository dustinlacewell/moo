package net.rizon.moo.conf;

public class DatabaseConfiguration extends Configuration
{
	public String connection;

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateNullOrNotEmpty("Database Connection", connection);
	}
}
