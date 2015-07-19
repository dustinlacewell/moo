package net.rizon.moo.plugin.tickets;

import java.util.Date;

class TicketTimer implements Runnable
{
	private TicketChecker c;

	@Override
	public void run()
	{
		if (c != null && c.isAlive())
			return;

		c = new TicketChecker();
		tickets.log.initThread(c);
		c.start();
	}
}