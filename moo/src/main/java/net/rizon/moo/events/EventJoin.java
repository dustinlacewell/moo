package net.rizon.moo.events;

import net.rizon.moo.Event;

public class EventJoin extends Event
{
	private final String source, channel;

	public EventJoin(String source, String channel)
	{
		this.source = source;
		this.channel = channel;
	}

	public String getSource()
	{
		return source;
	}

	public String getChannel()
	{
		return channel;
	}
}
