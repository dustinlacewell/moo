package net.rizon.moo.plugin.tickets;

import com.google.common.eventbus.Subscribe;
import io.netty.util.concurrent.ScheduledFuture;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.plugin.tickets.conf.TicketsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class tickets extends Plugin
{
	private static final Logger logger = LoggerFactory.getLogger(tickets.class);

	private ScheduledFuture ticketTimer;
	protected static HashMap<Integer, Ticket> tickets = new HashMap<Integer, Ticket>();
	public static TicketsConfiguration conf;

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
		Moo.getEventBus().register(this);
	}

	@Override
	public void stop()
	{
		ticketTimer.cancel(false);
		Moo.getEventBus().unregister(this);
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
}