package net.rizon.moo.plugin.tickets;

import java.util.Date;

import net.rizon.moo.Timer;

class TicketTimer extends Timer
{
	private TicketChecker c;
	
	public TicketTimer()
	{
		super(60, true);
	}

	@Override
	public void run(Date now)
	{
		if (c != null && c.isAlive())
			return;
		
		c = new TicketChecker();
		c.start();
	}
}