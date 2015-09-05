package net.rizon.moo.plugin.core;

import com.google.common.eventbus.Subscribe;
import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.plugin.core.conf.CoreConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class core extends Plugin
{
	private static final Logger logger = LoggerFactory.getLogger(core.class);
	
	public static CoreConfiguration conf;

	private CommandHelp help;
	private Command host, plugins, rand, reload, shell, shutdown, status;

	public core() throws Exception
	{
		super("Commands", "Core commands");
		conf = CoreConfiguration.load();
	}

	@Override
	public void start() throws Exception
	{
		help = new CommandHelp(this);
		host = new CommandHost(this);
		plugins = new CommandPlugins(this);
		rand = new CommandRand(this);
		reload = new CommandReload(this);
		shell = new CommandShell(this);
		shutdown = new CommandShutdown(this);
		status = new CommandStatus(this);

		Moo.getEventBus().register(this);
	}

	@Override
	public void stop()
	{
		help.remove();
		host.remove();
		plugins.remove();
		rand.remove();
		reload.remove();
		shell.remove();
		shutdown.remove();
		status.remove();
		
		Moo.getEventBus().unregister(this);
	}
	
	@Subscribe
	public void onReload(OnReload evt)
	{
		try
		{
			core.conf = CoreConfiguration.load();
		}
		catch (Exception ex)
		{
			evt.getSource().reply("Error reloading core configuration: " + ex.getMessage());
			
			logger.warn("Unable to reload core configuration", ex);
		}
	}
}
