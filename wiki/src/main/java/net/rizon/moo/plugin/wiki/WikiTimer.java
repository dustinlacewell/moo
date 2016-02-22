package net.rizon.moo.plugin.wiki;

import net.rizon.moo.Moo;
import net.rizon.moo.logging.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WikiTimer implements Runnable
{
	private static final Logger logger = LoggerFactory.getLogger(WikiTimer.class);
	
	private WikiChecker c;
	
	@Override
	public void run()
	{
		if (c != null && c.isAlive())
			return;

		c = new WikiChecker();
		Moo.injector.injectMembers(c);
		LoggerUtils.initThread(logger, c);
		c.start();
	}
}