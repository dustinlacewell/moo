package net.rizon.moo.conf;

public class NickServ extends Configuration
{
	public String pass, mask;

	/**
	 * Checks if the NickServ configuration is valid.
	 * @throws ConfigurationException When NickServ configuration is invalid.
	 */
	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateNotEmpty("NickServPass", pass);
		Validator.validateIRCMask("NickServMask", mask);
	}
}
