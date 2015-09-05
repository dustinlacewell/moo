package net.rizon.moo.events;

import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;

public class OnReload extends Event
{
	private final CommandSource source;

	public OnReload(CommandSource source)
	{
		this.source = source;
	}

	public CommandSource getSource()
	{
		return source;
	}
}
