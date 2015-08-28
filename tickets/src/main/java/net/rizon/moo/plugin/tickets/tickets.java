package net.rizon.moo.plugin.tickets;

import io.netty.util.concurrent.ScheduledFuture;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.tickets.conf.TicketsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class tickets extends Plugin
{
	private static final Logger logger = LoggerFactory.getLogger(tickets.class);

	private ScheduledFuture ticketTimer;
	private Event e;
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
		e = new EventTicket();
	}

	@Override
	public void stop()
	{
		ticketTimer.cancel(false);
		e.remove();
	}
}