package net.rizon.moo.events;

import net.rizon.moo.irc.Server;

public class OnXLineAdd
{
	private final Server server;
	private final char type;
	private final String value;

	public OnXLineAdd(Server server, char type, String value)
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
