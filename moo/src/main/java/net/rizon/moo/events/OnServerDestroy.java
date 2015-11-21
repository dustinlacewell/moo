package net.rizon.moo.events;

import net.rizon.moo.Event;
import net.rizon.moo.Server;

public class OnServerDestroy extends Event
{
	private final Server server;

	public OnServerDestroy(Server server)
	{
		this.server = server;
	}

	public Server getServer()
	{
		return server;
	}
}
