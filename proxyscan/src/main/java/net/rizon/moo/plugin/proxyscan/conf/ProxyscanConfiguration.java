package net.rizon.moo.plugin.proxyscan.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class ProxyscanConfiguration extends Configuration
{
	private ProxyscanServerConfiguration server;
	private ProxyscanConnectConfiguration connect;
	private String[] bindip;
	private String[] bindip6;
	private int expiry;
	private String path;
	private String arguments;
	private String check_string;
	private String ban_message;
	private String scan_notice;
	private boolean py_opers;
	private String[] channels;
	private DroneBL dronebl;

	/**
	 * Loads the Proxyscan Configuration settings.
	 *
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

		if (dronebl != null)
		{
			dronebl.validate();
		}

		Validator.validateHostList("Proxyscan bindipsv4", bindip);
		Validator.validateHostList("Proxyscan bindipsv6", bindip6);
		Validator.validateNotZero("expiry", expiry);
		Validator.validatePath("Proxyscan path", path);
		Validator.validateNullOrNotEmpty("Proxyscan arguments", arguments);
		Validator.validateNotEmpty("Proxyscan check_string", check_string);
		Validator.validateNotEmpty("Proxyscan ban_message", ban_message);
		Validator.validateNotNull("Proxyscan py_opers", py_opers);
		Validator.validateChannelList("Proxyscan channels", channels);
	}

	public ProxyscanServerConfiguration getServer()
	{
		return server;
	}

	public void setServer(ProxyscanServerConfiguration server)
	{
		this.server = server;
	}

	public ProxyscanConnectConfiguration getConnect()
	{
		return connect;
	}

	public void setConnect(ProxyscanConnectConfiguration connect)
	{
		this.connect = connect;
	}

	public String[] getBindip()
	{
		return bindip;
	}

	public void setBindip(String[] bindip)
	{
		this.bindip = bindip;
	}

	public String[] getBindip6()
	{
		return bindip6;
	}

	public void setBindip6(String[] bindip6)
	{
		this.bindip6 = bindip6;
	}

	public int getExpiry()
	{
		return expiry;
	}

	public void setExpiry(int expiry)
	{
		this.expiry = expiry;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getArguments()
	{
		return arguments;
	}

	public void setArguments(String arguments)
	{
		this.arguments = arguments;
	}

	public String getCheck_string()
	{
		return check_string;
	}

	public void setCheck_string(String check_string)
	{
		this.check_string = check_string;
	}

	public String getBan_message()
	{
		return ban_message;
	}

	public void setBan_message(String ban_message)
	{
		this.ban_message = ban_message;
	}

	public String getScan_notice()
	{
		return scan_notice;
	}

	public void setScan_notice(String scan_notice)
	{
		this.scan_notice = scan_notice;
	}

	public boolean isPy_opers()
	{
		return py_opers;
	}

	public void setPy_opers(boolean py_opers)
	{
		this.py_opers = py_opers;
	}

	public String[] getChannels()
	{
		return channels;
	}

	public void setChannels(String[] channels)
	{
		this.channels = channels;
	}

	public DroneBL getDronebl()
	{
		return dronebl;
	}

	public void setDronebl(DroneBL dronebl)
	{
		this.dronebl = dronebl;
	}
}
