package net.rizon.moo.events;

import net.rizon.moo.Event;

public class EventKick extends Event
{
	private final String source, target, channel;

	public EventKick(String source, String target, String channel)
	{
		this.source = source;
		this.target = target;
		this.channel = channel;
	}

	public String getSource()
	{
		return source;
	}

	public String getTarget()
	{
		return target;
	}

	public String getChannel()
	{
		return channel;
	}
}
