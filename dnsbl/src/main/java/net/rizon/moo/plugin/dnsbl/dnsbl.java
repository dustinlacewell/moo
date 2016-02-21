package net.rizon.moo.plugin.dnsbl;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import net.rizon.moo.Command;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.plugin.dnsbl.actions.Action;
import net.rizon.moo.plugin.dnsbl.actions.ActionAkill;
import net.rizon.moo.plugin.dnsbl.actions.ActionLog;
import net.rizon.moo.plugin.dnsbl.conf.DnsblConfiguration;
import org.slf4j.Logger;

public class dnsbl extends Plugin implements EventListener
{
	@Inject
	private static Logger logger;
	
	@Inject
	private CommandDnsbl command;

	@Inject
	private BlacklistManager blacklistManager;

	@Inject
	private Set<Action> actions;

	private DnsblConfiguration conf;

	public dnsbl() throws Exception
	{
		super("DNSBL", "Monitors connections for DNSBL hits and takes action.");
		conf = DnsblConfiguration.load();
	}

	@Override
	public void start()
	{
		DnsblChecker.load(conf);
		blacklistManager.load();
	}

	@Override
	public void stop()
	{
	}

	@Override
	public List<Command> getCommands()
	{
		return Arrays.<Command>asList(command);
	}

	@Override
	protected void configure()
	{
		bind(dnsbl.class).toInstance(this);
		
		bind(DnsblConfiguration.class).toInstance(conf);

		bind(BlacklistManager.class).toInstance(new BlacklistManager());
		bind(ResultCache.class).toInstance(new ResultCache());

		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder(), Command.class);
		commandBinder.addBinding().to(CommandDnsbl.class);

		Multibinder<EventListener> eventListenerBinder = Multibinder.newSetBinder(binder(), EventListener.class);
		eventListenerBinder.addBinding().to(EventDnsblCheck.class);

		Multibinder<Action> actionBinder = Multibinder.newSetBinder(binder(), Action.class);
		actionBinder.addBinding().to(ActionAkill.class);
		actionBinder.addBinding().to(ActionLog.class);
	}

	public Collection<Action> getAllActions()
	{
		return actions;
	}

	public Action getByName(String name)
	{
		for (Action a : getAllActions())
		{
			if (a.getName().equals(name))
			{
				return a;
			}
		}

		return null;
	}

	@Subscribe
	public void onReload(OnReload evt)
	{
		try
		{
			DnsblConfiguration c = DnsblConfiguration.load();
			conf = c;

			// Now the Guice graph gets rebuilt which reinjects conf everywhere, and then
			// re starts the plugin, which applies the configuration
		}
		catch (Exception ex)
		{
			evt.getSource().reply("Error reloading dnsbl configuration: " + ex.getMessage());

			logger.warn("Unable to reload dnsbl configuration", ex);
		}
	}
}
