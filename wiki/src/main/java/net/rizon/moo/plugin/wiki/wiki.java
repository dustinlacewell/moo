package net.rizon.moo.plugin.wiki;

import com.google.common.eventbus.Subscribe;
import com.google.inject.multibindings.Multibinder;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.plugin.wiki.conf.WikiConfiguration;
import org.slf4j.Logger;

public class wiki extends Plugin implements EventListener
{
	@Inject
	private static Logger logger;
	
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
		wiki = Moo.scheduleWithFixedDelay(new WikiTimer(), 1, TimeUnit.MINUTES);
	}

	@Override
	public void stop()
	{
		wiki.cancel(false);
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

	@Override
	public List<Command> getCommands()
	{
		return Arrays.asList();
	}

	@Override
	protected void configure()
	{
		bind(wiki.class).toInstance(this);
		
		Multibinder<EventListener> eventListenerBinder = Multibinder.newSetBinder(binder(), EventListener.class);
		eventListenerBinder.addBinding().toInstance(this);
	}
}