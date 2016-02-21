package net.rizon.moo.plugin.core;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import java.util.Arrays;
import java.util.List;
import net.rizon.moo.Command;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.plugin.core.conf.CoreConfiguration;
import org.slf4j.Logger;

public class core extends Plugin implements EventListener
{
	@Inject
	private static Logger logger;

	private CoreConfiguration conf;

	@Inject
	private CommandHelp help;

	@Inject
	private CommandHost host;

	@Inject
	private CommandPlugins plugins;

	@Inject
	private CommandRand rand;

	@Inject
	private CommandReload reload;

	@Inject
	private CommandShell shell;

	@Inject
	private CommandShutdown shutdown;

	@Inject
	private CommandStatus status;

	public core() throws Exception
	{
		super("Commands", "Core commands");
		conf = CoreConfiguration.load();
	}
	
	@Override
	protected void configure()
	{
		bind(core.class).toInstance(this);
		
		bind(CoreConfiguration.class).toInstance(conf);
		
		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder(), Command.class);
		commandBinder.addBinding().to(CommandHelp.class);
		commandBinder.addBinding().to(CommandHost.class);
		commandBinder.addBinding().to(CommandPlugins.class);
		commandBinder.addBinding().to(CommandRand.class);
		commandBinder.addBinding().to(CommandReload.class);
		commandBinder.addBinding().to(CommandShell.class);
		commandBinder.addBinding().to(CommandShutdown.class);
		commandBinder.addBinding().to(CommandStatus.class);

		Multibinder<EventListener> eventListenerBinder = Multibinder.newSetBinder(binder(), EventListener.class);
		eventListenerBinder.addBinding().toInstance(this);
	}

	@Override
	public void start() throws Exception
	{
	}

	@Override
	public void stop()
	{
	}
	
	@Subscribe
	public void onReload(OnReload evt)
	{
		try
		{
			conf = CoreConfiguration.load();
		}
		catch (Exception ex)
		{
			evt.getSource().reply("Error reloading core configuration: " + ex.getMessage());
			
			logger.warn("Unable to reload core configuration", ex);
		}
	}

	@Override
	public List<Command> getCommands()
	{
		return Arrays.asList(help, host, plugins, rand, reload, shell, shutdown, status);
	}
}
