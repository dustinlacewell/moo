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
	private String[] channels;
	private String[] admin_channels;
	private boolean shell;
	private String shell_base;
	private boolean disable_split_message;
	private int debug;

	public void load() throws Exception
	{
		Properties prop = new Properties();
		prop.load(new FileInputStream("moo.properties"));
		
		this.server = prop.getProperty("server");
		this.port = Integer.parseInt(prop.getProperty("port"));
		this.ssl = Boolean.parseBoolean(prop.getProperty("ssl"));
		this.nick = prop.getProperty("nick");
		this.ident = prop.getProperty("ident");
		this.host = prop.getProperty("host");
		this.realname = prop.getProperty("realname");
		this.server_pass = prop.getProperty("server_pass");
		this.version = prop.getProperty("version");
		this.nickserv_pass = prop.getProperty("nickserv_pass");
		this.geoserv_pass = prop.getProperty("geoserv_pass");
		this.oper = prop.getProperty("oper");
		String chan = prop.getProperty("channels");
		if (chan != null)
			this.channels = chan.split(",");
		chan = prop.getProperty("admin_channels");
		if (chan != null)
			this.admin_channels = chan.split(",");
		this.shell = Boolean.parseBoolean(prop.getProperty("enable_shell"));
		this.shell_base = prop.getProperty("shell_base");
		this.disable_split_message = Boolean.parseBoolean(prop.getProperty("disable_split_message"));
		this.debug = Integer.parseInt(prop.getProperty("debug"));
		
		this.check();
	}
	
	private void check() throws Exception
	{
		if (this.getServer() == null || this.getServer().isEmpty())
			throw new Exception("A server must be configured");
		else if (this.getPort() <= 0 || this.getPort() > 65535)
			throw new Exception("A valid port must be given");
		else if (this.getNick() == null || this.getNick().isEmpty())
			throw new Exception("A valid nick must be configured");
		else if (this.getIdent() == null || this.getIdent().isEmpty())
			throw new Exception("A valid ident must be configured");
		else if (this.getRealname() == null || this.getRealname().isEmpty())
			throw new Exception("A valid realname must be configured");
		else if (this.getVersion() == null || this.getVersion().isEmpty())
			throw new Exception("A valid version must be configured");
		else if (this.getShellBase() == null)
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
	
	public final String[] getChannels()
	{
		return this.channels;
	}

	public final String[] getAdminChannels()
	{
		return this.admin_channels;
	}
	
	public final boolean isAdminChannel(final String channel)
	{
		if (this.admin_channels != null)
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
	
	public final int getDebug()
	{
		return this.debug;
	}
}
