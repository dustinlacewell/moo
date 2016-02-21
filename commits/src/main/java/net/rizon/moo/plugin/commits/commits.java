package net.rizon.moo.plugin.commits;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import java.util.Arrays;
import java.util.List;
import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.logging.LoggerUtils;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.events.OnShutdown;
import net.rizon.moo.plugin.commits.conf.CommitsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class commits extends Plugin implements EventListener
{
	@Inject
	private static Logger logger;

	@Inject
	private Server s;
	
	private CommitsConfiguration conf;

	public commits() throws Exception
	{
		super("Commits", "Manages and shows commits made to repositories");
		conf = CommitsConfiguration.load();
	}

	@Override
	public void start() throws Exception
	{
		LoggerUtils.initThread(logger, s);
		s.start();
	}

	@Override
	public void stop()
	{
		s.stopServer();
	}
	
	@Subscribe
	public void onShutdown(OnShutdown evt)
	{
		s.shutdown();
	}

	/**
	 * Reloads the Configuration of commits.
	 */
	@Subscribe
	public void onReload(OnReload evt)
	{
		try
		{
			conf = CommitsConfiguration.load();
		}
		catch (Exception ex)
		{
			evt.getSource().reply("Error reloading commits configuration: " + ex.getMessage());
			
			commits.logger.warn("Unable to reload commits configuration", ex);
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
		bind(commits.class).toInstance(this);

		bind(Server.class);

		Multibinder<EventListener> eventListenerBinder = Multibinder.newSetBinder(binder(), EventListener.class);
		eventListenerBinder.addBinding().toInstance(this);
	}
}
