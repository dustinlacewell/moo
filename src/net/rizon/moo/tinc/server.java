package net.rizon.moo.tinc;

import java.util.LinkedList;

import net.rizon.moo.servercontrol.serverInfo;

public class server
{
	private serverInfo si;
	
	protected server(serverInfo si)
	{
		this.si = si;
	}
	
	public serverInfo getServerInfo()
	{
		return this.si;
	}
	
	public String getName()
	{
		return this.si.name;
	}
	
	private static LinkedList<server> servers = new LinkedList<server>();
	
	protected static server findOrCreateServer(serverInfo si)
	{
		for (server s : servers)
			if (s.getName().equalsIgnoreCase(si.name))
				return s;
				
		server s = new server(si);
		servers.add(s);
		return s;
	}
}