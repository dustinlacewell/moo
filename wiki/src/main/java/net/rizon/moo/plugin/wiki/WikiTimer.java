package net.rizon.moo.plugin.wiki;

import java.util.Date;

class WikiTimer implements Runnable
{
	private WikiChecker c;
	
	@Override
	public void run()
	{
		if (c != null && c.isAlive())
			return;

		c = new WikiChecker();
		wiki.log.initThread(c);
		c.start();
	}
}