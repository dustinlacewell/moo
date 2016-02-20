package net.rizon.moo.events;

import net.rizon.moo.Event;
import net.rizon.moo.irc.Server;

public class OnServerCreate extends Event
{
	private final Server server;

	public OnServerCreate(Server server)
	{
		this.server = server;
	}

	public Server getServer()
	{
		return server;
	}
}
