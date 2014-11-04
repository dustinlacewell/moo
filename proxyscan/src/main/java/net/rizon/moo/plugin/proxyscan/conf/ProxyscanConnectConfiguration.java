package net.rizon.moo.plugin.proxyscan.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class ProxyscanConnectConfiguration extends Configuration
{
	public String ip;
	public int port;

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateHost("Proxyscan Connect IP", ip);
		Validator.validatePort("Proxyscan Connect Port", port, true);
	}
}
