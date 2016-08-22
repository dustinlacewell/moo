package net.rizon.moo.plugin.proxyscan.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class ProxyscanConnectConfiguration extends Configuration
{
	private String ip;
	private int port;

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateHost("Proxyscan Connect IP", ip);
		Validator.validatePort("Proxyscan Connect Port", port, true);
	}

	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}
}
