package net.rizon.moo.plugin.osflood;

import net.rizon.moo.Event;
import net.rizon.moo.Plugin;

public class osflood extends Plugin
{
	private Event e;
	
	public osflood()
	{
		super("OSFlood", "Detects and akills users flooding OperServ");
	}

	@Override
	public void start() throws Exception
	{
		e = new EventOSFlood();
	}

	@Override
	public void stop()
	{
		e.remove();
	}
	
}
