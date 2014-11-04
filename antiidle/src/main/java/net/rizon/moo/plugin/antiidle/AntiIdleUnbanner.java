package net.rizon.moo.plugin.antiidle;

import java.util.Date;

import net.rizon.moo.Moo;
import net.rizon.moo.Timer;

class AntiIdleUnbanner extends Timer
{
	private String host;
	
	public AntiIdleUnbanner(final String mask)
	{
		super(antiidle.conf.bantime * 60, false);
	
		String host = mask;
		int a = mask.indexOf('@');
		if (a != -1)
			host = "*!*@" + mask.substring(a + 1);
		this.host = host;
		
		Moo.mode(antiidle.conf.channel, "+b " + this.host);
	}

	@Override
	public void run(Date now)
	{
		Moo.mode(antiidle.conf.channel, "-b " + this.host);
	}
}