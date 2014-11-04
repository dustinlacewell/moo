package net.rizon.moo.plugin.servermonitor.conf;

import java.util.List;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class ServerMonitorConfiguration extends Configuration
{
	public String domain;
	public String[] check;
	public boolean reconnect;
	public boolean messages;
	public int port;
	public List<String> split_emails;

	/**
	 * Loads the ServerMonitor Configuration settings.
	 * @return Configuration settings.
	 * @throws Exception Thrown when something goes wrong.
	 */
	public static ServerMonitorConfiguration load() throws Exception
	{
		return load("servermonitor.yml", ServerMonitorConfiguration.class);
	}

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateHost("ServerMonitor domain", domain);
		Validator.validateHostList("ServerMonitor check", check);
		Validator.validateNotNull("ServerMonitor reconnect", reconnect);
		Validator.validateNotNull("ServerMonitor messages", messages);
		Validator.validatePort("ServerMonitor Reconnect port", port, true);
		Validator.validateEmailList("ServerMonitor Split emails", split_emails);
	}
}
