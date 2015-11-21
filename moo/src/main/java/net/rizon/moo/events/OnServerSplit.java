package net.rizon.moo.events;

import net.rizon.moo.Server;

public class OnServerSplit
{
	private final Server server, from;

	public OnServerSplit(Server server, Server from)
	{
		this.server = server;
		this.from = from;
	}

	public Server getServer()
	{
		return server;
	}

	public Server getFrom()
	{
		return from;
	}
}
