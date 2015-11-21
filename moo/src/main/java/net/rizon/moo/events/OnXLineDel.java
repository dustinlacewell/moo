package net.rizon.moo.events;

import net.rizon.moo.Server;

public class OnXLineDel
{
	private final Server server;
	private final char type;
	private final String value;

	public OnXLineDel(Server server, char type, String value)
	{
		this.server = server;
		this.type = type;
		this.value = value;
	}

	public Server getServer()
	{
		return server;
	}

	public char getType()
	{
		return type;
	}

	public String getValue()
	{
		return value;
	}
}
