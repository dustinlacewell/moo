package net.rizon.moo.servercontrol;

import java.util.HashMap;

public abstract class protocol
{
	public protocol(final String name)
	{
		protocols.put(name.toLowerCase(), this);
	}
	
	public abstract connection createConnection();
	
	private static HashMap<String, protocol> protocols = new HashMap<String, protocol>();
	
	public static protocol findProtocol(final String name)
	{
		return protocols.get(name.toLowerCase());
	}
}