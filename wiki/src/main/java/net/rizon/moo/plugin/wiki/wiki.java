package net.rizon.moo.plugin.wiki;

import com.google.common.eventbus.Subscribe;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.plugin.wiki.conf.WikiConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class wiki extends Plugin
{
	private static final Logger logger = LoggerFactory.getLogger(wiki.class);
	
	private ScheduledFuture wiki;
	public static WikiConfiguration conf;

	public wiki() throws Exception
	{
		super("Wiki", "Monitors the Wiki and reports changes");
		conf = WikiConfiguration.load();
	}

	@Override
	public void start() throws Exception
	{
		Moo.getEventBus().register(this);
		wiki = Moo.scheduleWithFixedDelay(new WikiTimer(), 1, TimeUnit.MINUTES);
	}

	@Override
	public void stop()
	{
		wiki.cancel(false);
		Moo.getEventBus().unregister(this);
	}
	
	@Subscribe
	public void onReload(OnReload evt)
	{
		// TODO: Config gets reloaded, but it's not used. Current implementation requires restart.
		try
		{
			conf = WikiConfiguration.load();
		}
		catch (Exception ex)
		{
			evt.getSource().reply("Error reloading wiki configuration: " + ex.getMessage());
			
			logger.warn("Unable to reload configuration", ex);
		}
	}
}