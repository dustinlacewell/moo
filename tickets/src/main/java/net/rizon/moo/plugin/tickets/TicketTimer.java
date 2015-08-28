package net.rizon.moo.plugin.tickets;

import net.rizon.moo.logging.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TicketTimer implements Runnable
{
	private static final Logger logger = LoggerFactory.getLogger(TicketTimer.class);
	
	private TicketChecker c;

	@Override
	public void run()
	{
		if (c != null && c.isAlive())
			return;

		c = new TicketChecker();
		LoggerUtils.initThread(logger, c);
		c.start();
	}
}