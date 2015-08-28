package net.rizon.moo.plugin.osflood;

import net.rizon.moo.Event;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.osflood.conf.OsfloodConfiguration;

public class osflood extends Plugin
{
	private Event e;
	public static OsfloodConfiguration conf;

	public osflood() throws Exception
	{
		super("OSFlood", "Detects and akills users flooding OperServ");
		conf = OsfloodConfiguration.load();
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
