package net.rizon.moo.plugin.tickets;

import net.rizon.moo.Event;
import net.rizon.moo.Logger;
import net.rizon.moo.Plugin;
import net.rizon.moo.Timer;
import net.rizon.moo.plugin.tickets.conf.TicketsConfiguration;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;

public class tickets extends Plugin
{
	protected static final Logger log = Logger.getLogger(tickets.class.getName());

	private Timer tickTimer;
	private Event e;
	protected static HashMap<Integer, Ticket> tickets = new HashMap<Integer, Ticket>();
	public static TicketsConfiguration conf;

	public tickets() throws Exception
	{
		super("Tickets", "Monitors abuse tickets for changes");
		conf = TicketsConfiguration.load();

		// This allows us to store cookies so we can properly login via HTTP POST
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

		tickTimer = new TicketTimer();
	}

	@Override
	public void start() throws Exception
	{
		tickTimer.start();
		e = new EventTicket();
	}

	@Override
	public void stop()
	{
		tickTimer.stop();
		e.remove();
	}
}