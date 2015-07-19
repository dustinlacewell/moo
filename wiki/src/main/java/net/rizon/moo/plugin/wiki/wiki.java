package net.rizon.moo.plugin.wiki;

import io.netty.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.rizon.moo.Event;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.wiki.conf.WikiConfiguration;

public class wiki extends Plugin
{
	protected static final Logger log = Logger.getLogger(wiki.class.getName());

	private ScheduledFuture wiki;
	private Event e;
	public static WikiConfiguration conf;

	public wiki() throws Exception
	{
		super("Wiki", "Monitors the Wiki and reports changes");
		conf = WikiConfiguration.load();
	}

	@Override
	public void start() throws Exception
	{
		e = new EventWiki();
		wiki = Moo.scheduleWithFixedDelay(new WikiTimer(), 1, TimeUnit.MINUTES);
	}

	@Override
	public void stop()
	{
		wiki.cancel(false);
		e.remove();
	}
}