package net.rizon.moo.events;

import net.rizon.moo.Event;
import net.rizon.moo.Server;

public class OnServerLink extends Event
{
	private final Server server, to;

	public OnServerLink(Server server, Server to)
	{
		this.server = server;
		this.to = to;
	}

	public Server getServer()
	{
		return server;
	}

	public Server getTo()
	{
		return to;
	}
}
