package net.rizon.moo.events;

import net.rizon.moo.Event;

public class EventWallops extends Event
{
	private final String source, message;

	public EventWallops(String source, String message)
	{
		this.source = source;
		this.message = message;
	}

	public String getSource()
	{
		return source;
	}

	public String getMessage()
	{
		return message;
	}
}
