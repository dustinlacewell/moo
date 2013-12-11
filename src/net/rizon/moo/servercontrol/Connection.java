package net.rizon.moo.servercontrol;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import net.rizon.moo.Timer;

final class connectionTimer extends Timer
{
	public connectionTimer()
	{
		super(300, true);
	}
	
	public void run(final Date now)
	{
		Connection[] cons = Connection.getConnections();
		
		if (cons.length == 0)
		{
			this.stop();
			timer = null;
			return;
		}
		
		for (Connection con : cons)
			if (con.processes.isEmpty())
				con.destroy();
	}
	
	public static connectionTimer timer;
}

public abstract class Connection
{
	private Protocol proto;
	public LinkedList<Process> processes = new LinkedList<Process>();
	private ServerInfo info = null;

	public Connection(Protocol proto, ServerInfo si)
	{
		this.proto = proto;
		this.info = si;
		
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
	
	public Protocol getProtocol()
	{
		return this.proto;
	}
	
	public ServerInfo getServerInfo()
	{
		return this.info;
	}
	
	public abstract boolean isConnected();
	public abstract void connect() throws IOException;
	public abstract void execute(final String command) throws IOException;
	public void upload(File file) throws IOException { throw new UnsupportedOperationException(); }
	public void remove(String file) throws IOException { throw new UnsupportedOperationException(); }
	public abstract String readLine();
	
	private static LinkedList<Connection> connections = new LinkedList<Connection>();
	
	public static Connection findConnection(ServerInfo si)
	{
		for (Iterator<Connection> it = connections.iterator(); it.hasNext();)
		{
			Connection con = it.next();
			if (con.getServerInfo().name.equalsIgnoreCase(si.name) && con.getProtocol().getProtocolName().equalsIgnoreCase(si.protocol))
				return con;
		}
		
		return null;
	}
	
	public static Connection findOrCreateConncetion(ServerInfo si)
	{
		Connection c = findConnection(si);
		if (c != null)
			return c;
		
		Protocol p = Protocol.findProtocol(si.protocol);
		return p.createConnection(si);
	}
	
	public static final Connection[] getConnections()
	{
		Connection[] cons = new Connection[connections.size()];
		connections.toArray(cons);
		return cons;
	}
}