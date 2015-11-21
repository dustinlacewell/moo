package net.rizon.moo.plugin.commits;

import com.google.common.eventbus.Subscribe;
import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.logging.LoggerUtils;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.events.OnShutdown;
import net.rizon.moo.plugin.commits.conf.CommitsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class commits extends Plugin
{
	protected static final Logger logger = LoggerFactory.getLogger(commits.class);

	protected static Server s;
	private Event e;
	public static CommitsConfiguration conf;

	public commits() throws Exception
	{
		super("Commits", "Manages and shows commits made to repositories");
		conf = CommitsConfiguration.load();
	}

	@Override
	public void start() throws Exception
	{
		s = new Server(conf.ip, conf.port);
		LoggerUtils.initThread(logger, s);
		s.start();

		Moo.getEventBus().register(this);
	}

	@Override
	public void stop()
	{
		s.stopServer();
		
		Moo.getEventBus().unregister(this);
	}
	
	@Subscribe
	public void onShutdown(OnShutdown evt)
	{
		commits.s.shutdown();
	}

	/**
	 * Reloads the Configuration of commits.
	 * @param source Origin of the target that the !RELOAD command originated from.
	 */
	@Subscribe
	public void onReload(OnReload evt)
	{
		try
		{
			commits.conf = CommitsConfiguration.load();
		}
		catch (Exception ex)
		{
			evt.getSource().reply("Error reloading commits configuration: " + ex.getMessage());
			
			commits.logger.warn("Unable to reload commits configuration", ex);
		}
	}
}
