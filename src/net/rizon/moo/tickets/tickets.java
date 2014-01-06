package net.rizon.moo.tickets;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;

import net.rizon.moo.Plugin;
import net.rizon.moo.Timer;

public class tickets extends Plugin
{
	private Timer tickTimer;
	
	protected static HashMap<Integer, Ticket> tickets = new HashMap<Integer, Ticket>();
	
	public tickets()
	{
		super("Tickets", "Monitors abuse tickets for changes");
		
		// This allows us to store cookies so we can properly login via HTTP POST
		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
		
		tickTimer = new TicketTimer();
	}

	@Override
	public void start() throws Exception
	{
		tickTimer.start();
	}

	@Override
	public void stop()
	{
		tickTimer.stop();
	}
}