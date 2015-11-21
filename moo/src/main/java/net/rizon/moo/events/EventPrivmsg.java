package net.rizon.moo.events;

import net.rizon.moo.Event;

public class EventPrivmsg extends Event
{
	private final String source, channel, message;

	public EventPrivmsg(String source, String channel, String message)
	{
		this.source = source;
		this.channel = channel;
		this.message = message;
	}

	public String getSource()
	{
		return source;
	}

	public String getChannel()
	{
		return channel;
	}

	public String getMessage()
	{
		return message;
	}
}
