package net.rizon.moo.events;

public class EventNotice
{
	private String source, channel, message;

	public EventNotice(String source, String channel, String message)
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
