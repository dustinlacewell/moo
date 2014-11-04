package net.rizon.moo.conf;

public class Mail extends Configuration
{
	public String path;

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateNotEmpty("Mail path", path);
	}
}
