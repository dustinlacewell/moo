package net.rizon.moo.plugin.tinc;

import java.util.LinkedList;

import net.rizon.moo.plugin.servercontrol.ServerInfo;

public class Server
{
	private ServerInfo si;
	
	protected Server(ServerInfo si)
	{
		this.si = si;
	}
	
	public ServerInfo getServerInfo()
	{
		return this.si;
	}
	
	public String getName()
	{
		return this.si.name;
	}
	
	private static LinkedList<Server> servers = new LinkedList<Server>();
	
	protected static Server findOrCreateServer(ServerInfo si)
	{
		for (Server s : servers)
			if (s.getName().equalsIgnoreCase(si.name))
				return s;
				
		Server s = new Server(si);
		servers.add(s);
		return s;
	}
}