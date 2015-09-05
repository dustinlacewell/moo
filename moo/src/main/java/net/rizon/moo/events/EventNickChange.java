package net.rizon.moo.events;

import net.rizon.moo.Event;

public class EventNickChange extends Event
{
	private final String source, dest;

	public EventNickChange(String source, String dest)
	{
		this.source = source;
		this.dest = dest;
	}

	public String getSource()
	{
		return source;
	}

	public String getDest()
	{
		return dest;
	}
}
