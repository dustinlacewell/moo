package net.rizon.moo;

import java.io.FileInputStream;
import java.util.Properties;

public class config
{
	private String server;
	private int port;
	private boolean ssl;
	private String nick;
	private String ident;
	private String host;
	private String realname;
	private String server_pass;
	private String version;
	private String nickserv_pass;
	private String geoserv_pass;
	private String oper;
	private String[] idle_channels;
	private String[] channels;
	private String[] split_channels;
	private String[] admin_channels;
	private boolean shell;
	private String shell_base;
	private boolean disable_split_message;
	private String sendmail_path;
	private String split_email;
	private String database;
	private int debug;
	
	private String getProperty(Properties prop, final String name)
	{
		final String value = prop.getProperty(name);
		return value != null ? value : "";
	}

	public void load() throws Exception
	{
		Properties prop = new Properties();
		prop.load(new FileInputStream("moo.properties"));
		
		this.server = this.getProperty(prop, "server");
		this.port = Integer.parseInt(this.getProperty(prop, "port"));
		this.ssl = Boolean.parseBoolean(this.getProperty(prop, "ssl"));
		this.nick = this.getProperty(prop, "nick");
		this.ident = this.getProperty(prop, "ident");
		this.host = this.getProperty(prop, "host");
		this.realname = this.getProperty(prop, "realname");
		this.server_pass = this.getProperty(prop, "server_pass");
		this.version = this.getProperty(prop, "version");
		this.nickserv_pass = this.getProperty(prop, "nickserv_pass");
		this.geoserv_pass = this.getProperty(prop, "geoserv_pass");
		this.oper = this.getProperty(prop, "oper");
		String chan;
		chan = this.getProperty(prop, "idle_channels");
		this.idle_channels = chan.split(",");
		chan = this.getProperty(prop, "channels");
		this.channels = chan.split(",");
		chan = this.getProperty(prop, "split_channels");
		this.split_channels = chan.split(" ");
		chan = this.getProperty(prop, "admin_channels");
		this.admin_channels = chan.split(",");
		this.shell = Boolean.parseBoolean(this.getProperty(prop, "enable_shell"));
		this.shell_base = this.getProperty(prop, "shell_base");
		this.disable_split_message = Boolean.parseBoolean(this.getProperty(prop, "disable_split_message"));
		this.sendmail_path = this.getProperty(prop, "sendmail_path");
		this.split_email = this.getProperty(prop, "split_email");
		this.database = this.getProperty(prop, "database");
		this.debug = Integer.parseInt(this.getProperty(prop, "debug"));
		
		this.check();
	}
	
	private void check() throws Exception
	{
		if (this.getServer().isEmpty())
			throw new Exception("A server must be configured");
		else if (this.getPort() <= 0 || this.getPort() > 65535)
			throw new Exception("A valid port must be given");
		else if (this.getNick().isEmpty())
			throw new Exception("A valid nick must be configured");
		else if (this.getIdent().isEmpty())
			throw new Exception("A valid ident must be configured");
		else if (this.getRealname().isEmpty())
			throw new Exception("A valid realname must be configured");
		else if (this.getVersion().isEmpty())
			throw new Exception("A valid version must be configured");
		else if (this.getShellBase().isEmpty())
			throw new Exception("A valid shell base must be configured");
	}
	
	public final String getServer()
	{
		return this.server;
	}
	
	public final int getPort()
	{
		return this.port;
	}
	
	public final boolean getSSL()
	{
		return this.ssl;
	}
	
	public final String getNick()
	{
		return this.nick;
	}
	
	public final String getIdent()
	{
		return this.ident;
	}
	
	public final String getHost()
	{
		return this.host;
	}
	
	public final String getRealname()
	{
		return this.realname;
	}
	
	public final String getServerPass()
	{
		return this.server_pass;
	}
	
	public final String getVersion()
	{
		return this.version;
	}
	
	public final String getNickServPass()
	{
		return this.nickserv_pass;
	}
	
	public final String getGeoServPass()
	{
		return this.geoserv_pass;
	}
	
	public final String getOper()
	{
		return this.oper;
	}
	
	public final boolean isIdleChannel(final String channel)
	{
		for (int i = 0; i < this.idle_channels.length; ++i)
			if (this.idle_channels[i].equalsIgnoreCase(channel))
				return true;
		return false;
	}
	
	public final String[] getIdleChannels()
	{
		return this.idle_channels;
	}
	
	public final String[] getChannels()
	{
		return this.channels;
	}
	
	public final String[] getSplitChannels()
	{
		return this.split_channels;
	}

	public final String[] getAdminChannels()
	{
		return this.admin_channels;
	}
	
	public final boolean isAdminChannel(final String channel)
	{
		for (int i = 0; i < this.admin_channels.length; ++i)
			if (this.admin_channels[i].equalsIgnoreCase(channel))
				return true;
		return false;
	}
	
	public boolean getShell()
	{
		return this.shell;
	}
	
	public final String getShellBase()
	{
		return this.shell_base;
	}
	
	public final boolean getDisableSplitMessage()
	{
		return this.disable_split_message;
	}
	
	public final String getSendmailPath()
	{
		return this.sendmail_path;
	}
	
	public final String getSplitEmail()
	{
		return this.split_email;
	}
	
	public final String getDatabase()
	{
		return this.database;
	}
	
	public final int getDebug()
	{
		return this.debug;
	}
}
