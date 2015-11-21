package net.rizon.moo.events;

import net.rizon.moo.Event;

public class EventMode extends Event
{
	private final String source, channel, modes;

	public EventMode(String source, String channel, String modes)
	{
		this.source = source;
		this.channel = channel;
		this.modes = modes;
	}

	public String getSource()
	{
		return source;
	}

	public String getChannel()
	{
		return channel;
	}

	public String getModes()
	{
		return modes;
	}
}
