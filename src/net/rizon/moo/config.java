package net.rizon.moo;

import java.io.FileInputStream;
import java.io.IOException;
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
	private int debug;

	public void load() throws IOException
	{
		Properties prop = new Properties();
		prop.load(new FileInputStream("moo.properties.template"));
		
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
		this.debug = Integer.parseInt(prop.getProperty("debug"));
		
		this.check();
	}
	
	private void check() throws IOException
	{
		if (this.getServer() == null || this.getServer().isEmpty())
			throw new IOException("A server must be configured");
		else if (this.getPort() <= 0 || this.getPort() > 65535)
			throw new IOException("A valid port must be given");
		else if (this.getNick() == null || this.getNick().isEmpty())
			throw new IOException("A valid nick must be configured");
		else if (this.getIdent() == null || this.getIdent().isEmpty())
			throw new IOException("A valid ident must be configured");
		else if (this.getRealname() == null || this.getRealname().isEmpty())
			throw new IOException("A valid realname must be configured");
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
	
	public final int getDebug()
	{
		return this.debug;
	}
}
