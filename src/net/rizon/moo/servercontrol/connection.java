package net.rizon.moo.servercontrol;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import net.rizon.moo.timer;

final class connectionTimer extends timer
{
	public connectionTimer()
	{
		super(300, true);
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
	private serverInfo info = null;

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
	
	public void setServerInfo(serverInfo info)
	{
		this.info = info;
	}
	
	public serverInfo getServerInfo()
	{
		return this.info;
	}
	
	public abstract boolean isConnected();
	public abstract void connect() throws IOException;
	public abstract void execute(final String command) throws IOException;
	public abstract String readLine();
	
	private static LinkedList<connection> connections = new LinkedList<connection>();
	
	public static connection findProcess(final String name, final String protocol)
	{
		for (Iterator<connection> it = connections.iterator(); it.hasNext();)
		{
			connection con = it.next();
			if (con.getServerInfo().name.equalsIgnoreCase(name) && con.getProtocol().getProtocolName().equalsIgnoreCase(protocol))
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