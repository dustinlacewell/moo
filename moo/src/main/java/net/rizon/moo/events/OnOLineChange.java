package net.rizon.moo.events;

import net.rizon.moo.Event;
import net.rizon.moo.irc.Server;

public class OnOLineChange extends Event
{
	private Server server;
	private String oper, diff;

	public OnOLineChange(Server server, String oper, String diff)
	{
		this.server = server;
		this.oper = oper;
		this.diff = diff;
	}

	public Server getServer()
	{
		return server;
	}

	public String getOper()
	{
		return oper;
	}

	public String getDiff()
	{
		return diff;
	}
}
