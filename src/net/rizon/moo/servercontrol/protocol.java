package net.rizon.moo.servercontrol;

import java.util.HashMap;

public abstract class protocol
{
	private String name;

	public protocol(final String name)
	{
		protocols.put(name.toLowerCase(), this);
		this.name = name;
	}
	
	public final String getProtocolName()
	{
		return this.name;
	}
	
	public abstract connection createConnection(serverInfo si);
	
	private static HashMap<String, protocol> protocols = new HashMap<String, protocol>();
	
	public static protocol findProtocol(final String name)
	{
		return protocols.get(name.toLowerCase());
	}
}