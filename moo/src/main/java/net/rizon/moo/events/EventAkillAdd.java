package net.rizon.moo.events;

import net.rizon.moo.Event;

public class EventAkillAdd extends Event
{
	private String setter, ip, reason;

	public EventAkillAdd(String setter, String ip, String reason)
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
