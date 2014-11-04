package net.rizon.moo.conf;

public class Oper extends Configuration
{
	public String name, pass;

	/**
	 * Checks if the Oper configuration is valid.
	 * @throws ConfigurationException
	 */
	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateNotEmpty("OperName", name);
		Validator.validateNotEmpty("OperPass", pass);
	}
}
