package net.rizon.moo.plugin.wiki;

import java.util.Date;

import net.rizon.moo.Timer;

class WikiTimer extends Timer
{
	private WikiChecker c;
	
	public WikiTimer()
	{
		super(60, true);
	}

	@Override
	public void run(Date now)
	{
		if (c != null && c.isAlive())
			return;
		
		c = new WikiChecker();
		c.start();
	}
}