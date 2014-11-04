package net.rizon.moo.plugin.proxyscan.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class ProxyscanConfiguration extends Configuration
{
	public ProxyscanServerConfiguration server;
	public ProxyscanConnectConfiguration connect;
	public String[] bindip;
	public String path, arguments, check_string, ban_message, scan_notice;
	public boolean py_opers;
	public String[] channels;

	/**
	 * Loads the Proxyscan Configuration settings.
	 * @return Configuration settings.
	 * @throws Exception Thrown when something goes wrong.
	 */
	public static ProxyscanConfiguration load() throws Exception
	{
		return load("proxyscan.yml", ProxyscanConfiguration.class);
	}

	@Override
	public void validate() throws ConfigurationException
	{
		server.validate();
		connect.validate();

		Validator.validateHostList("Proxyscan bindips", bindip);
		Validator.validatePath("Proxyscan path", path);
		Validator.validateNullOrNotEmpty("Proxyscan arguments", arguments);
		Validator.validateNotEmpty("Proxyscan check_string", check_string);
		Validator.validateNotEmpty("Proxyscan ban_message", ban_message);
		Validator.validateNotEmpty("Proxyscan scan_notice", scan_notice);
		Validator.validateNotNull("Proxyscan py_opers", py_opers);
		Validator.validateChannelList("Proxyscan channels", channels);
	}
}
