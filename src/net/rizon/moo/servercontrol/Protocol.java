package net.rizon.moo.servercontrol;

import java.util.HashMap;

public abstract class Protocol
{
	private String name;

	public Protocol(final String name)
	{
		protocols.put(name.toLowerCase(), this);
		this.name = name;
	}
	
	public final String getProtocolName()
	{
		return this.name;
	}
	
	public abstract Connection createConnection(ServerInfo si);
	
	private static HashMap<String, Protocol> protocols = new HashMap<String, Protocol>();
	
	public static Protocol findProtocol(final String name)
	{
		return protocols.get(name.toLowerCase());
	}
}