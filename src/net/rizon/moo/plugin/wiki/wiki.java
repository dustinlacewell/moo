package net.rizon.moo.plugin.wiki;

import net.rizon.moo.Plugin;
import net.rizon.moo.Timer;

public class wiki extends Plugin
{
	private Timer wiki;
	
	public wiki()
	{
		super("Wiki", "Monitors the Wiki and reports changes");
		
		wiki = new WikiTimer();
	}

	@Override
	public void start() throws Exception
	{
		wiki.start();
	}

	@Override
	public void stop()
	{
		wiki.stop();
	}
}