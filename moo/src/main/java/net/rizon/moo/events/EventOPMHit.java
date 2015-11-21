package net.rizon.moo.events;

import net.rizon.moo.Event;

public class EventOPMHit extends Event
{
	private final String nick, ip, reason;

	public EventOPMHit(String nick, String ip, String reason)
	{
		this.nick = nick;
		this.ip = ip;
		this.reason = reason;
	}

	public String getNick()
	{
		return nick;
	}

	public String getIp()
	{
		return ip;
	}

	public String getReason()
	{
		return reason;
	}
}
