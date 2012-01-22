package net.rizon.moo.servercontrol;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import net.rizon.moo.timer;

final class connectionTimer extends timer
{
	public connectionTimer()
	{
		super(60, true);
	}
	
	public void run(final Date now)
	{
		connection[] cons = connection.getConnections();
		
		if (cons.length == 0)
		{
			this.stop();
			timer = null;
			return;
		}
		
		for (connection con : cons)
			if (con.processes.isEmpty())
				con.destroy();
	}
	
	public static connectionTimer timer;
}

public abstract class connection
{
	private protocol proto;
	public LinkedList<process> processes = new LinkedList<process>(); 
	private String host;
	private int port;
	private String user;
	private String password;
	
	public connection(protocol proto)
	{
		this.proto = proto;
		connections.add(this);
		
		if (connectionTimer.timer == null)
		{
			connectionTimer.timer = new connectionTimer();
			connectionTimer.timer.start();
		}
	}
	
	public void destroy()
	{
		connections.remove(this);
	}
	
	public protocol getProtocol()
	{
		return this.proto;
	}

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
	public abstract void execute(final String command) throws Exception;
	public abstract String readLine();
	
	private static LinkedList<connection> connections = new LinkedList<connection>();
	
	public static connection findProcess(final String host, final String protocol)
	{
		for (Iterator<connection> it = connections.iterator(); it.hasNext();)
		{
			connection con = it.next();
			if (con.getHost().equalsIgnoreCase(host) && con.getProtocol().getProtocolName().equalsIgnoreCase(protocol))
				return con;
		}
		
		return null;
	}
	
	public static final connection[] getConnections()
	{
		connection[] cons = new connection[connections.size()];
		connections.toArray(cons);
		return cons;
	}
}