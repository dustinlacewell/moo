package net.rizon.moo.plugin.tickets;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import io.netty.util.concurrent.ScheduledFuture;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.rizon.moo.Command;

import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.plugin.tickets.conf.TicketsConfiguration;
import org.slf4j.Logger;

public class tickets extends Plugin implements EventListener
{
	@Inject
	private static Logger logger;

	private ScheduledFuture ticketTimer;
	
	static Map<Integer, Ticket> tickets = new HashMap<>();
	private TicketsConfiguration conf;

	public tickets() throws Exception
	{
		super("Tickets", "Monitors abuse tickets for changes");
		conf = TicketsConfiguration.load();

		// This allows us to store cookies so we can properly login via HTTP POST
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
	}

	@Override
	public void start() throws Exception
	{
		ticketTimer = Moo.scheduleWithFixedDelay(new TicketTimer(), 1, TimeUnit.MINUTES);
	}

	@Override
	public void stop()
	{
		ticketTimer.cancel(false);
	}
	
	@Subscribe
	public void onReload(OnReload evt)
	{
		try
		{
			conf = TicketsConfiguration.load();
		}
		catch (Exception ex)
		{
			evt.getSource().reply("Error reloading tickets configuration: " + ex.getMessage());
			
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
		bind(tickets.class).toInstance(this);
		bind(TicketsConfiguration.class).toInstance(conf);
		
		Multibinder<EventListener> eventListenerBinder = Multibinder.newSetBinder(binder(), EventListener.class);
		eventListenerBinder.addBinding().toInstance(this);
	}
}