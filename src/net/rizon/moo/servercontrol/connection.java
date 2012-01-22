package net.rizon.moo.servercontrol;

public abstract class connection
{
	private String host;
	private int port;
	private String user;
	private String password;

	public connection setHost(final String host)
	{
		this.host = host;
		return this;
	}
	
	public final String getHost()
	{
		return this.host;
	}
	
	public connection setPort(int port)
	{
		this.port = port;
		return this;
	}
	
	public int getPort()
	{
		return this.port;
	}
	
	public connection setUser(final String user)
	{
		this.user = user;
		return this;
	}
	
	public final String getUser()
	{
		return this.user;
	}
	
	public connection setPassword(final String password)
	{
		this.password = password;
		return this;
	}
	
	public final String getPassword()
	{
		return this.password;
	}
	
	public abstract void connect() throws Exception;
	public abstract void execute(final String command);
	public abstract String readLine();
	public abstract void destroy();
}