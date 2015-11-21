package net.rizon.moo.events;

import net.rizon.moo.Event;

public class EventQuit extends Event
{
	private final String source, reason;

	public EventQuit(String source, String reason)
	{
		this.source = source;
		this.reason = reason;
	}

	public String getSource()
	{
		return source;
	}

	public String getReason()
	{
		return reason;
	}
}
