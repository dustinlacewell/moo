package net.rizon.moo.events;

import net.rizon.moo.Event;

public class EventAkillDel extends Event
{
	private final String setter, ip, reason;

	public EventAkillDel(String setter, String ip, String reason)
	{
		this.setter = setter;
		this.ip = ip;
		this.reason = reason;
	}

	public String getSetter()
	{
		return setter;
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
