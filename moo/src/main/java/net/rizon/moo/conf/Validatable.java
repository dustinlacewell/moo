package net.rizon.moo.conf;

public interface Validatable
{
	/**
	 * Validates the configuration.
	 * @throws ConfigurationException When there is an invalid setting.
	 */
	public void validate() throws ConfigurationException;
}
