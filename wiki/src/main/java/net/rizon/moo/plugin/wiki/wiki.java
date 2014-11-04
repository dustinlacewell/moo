package net.rizon.moo.plugin.wiki;

import net.rizon.moo.Event;
import net.rizon.moo.Logger;
import net.rizon.moo.Plugin;
import net.rizon.moo.Timer;
import net.rizon.moo.plugin.wiki.conf.WikiConfiguration;

public class wiki extends Plugin
{
	protected static final Logger log = Logger.getLogger(wiki.class.getName());

	private Timer wiki;
	private Event e;
	public static WikiConfiguration conf;
	
	public wiki() throws Exception
	{
		super("Wiki", "Monitors the Wiki and reports changes");
		conf = WikiConfiguration.load();
		wiki = new WikiTimer();
	}

	@Override
	public void start() throws Exception
	{
		wiki.start();
		e = new EventWiki();
	}

	@Override
	public void stop()
	{
		wiki.stop();
		e.remove();
	}
}