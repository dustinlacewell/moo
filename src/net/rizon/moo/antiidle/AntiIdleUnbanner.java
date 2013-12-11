package net.rizon.moo.antiidle;

import java.util.Date;

import net.rizon.moo.Moo;
import net.rizon.moo.Timer;

class AntiIdleUnbanner extends Timer
{
	private String host;
	
	public AntiIdleUnbanner(final String mask)
	{
		super(Moo.conf.getAntiIdleBanTime() * 60, false);
	
		String host = mask;
		int a = mask.indexOf('@');
		if (a != -1)
			host = "*!*@" + mask.substring(a + 1);
		this.host = host;
		
		Moo.mode(Moo.conf.getAntiIdleChannel(), "+b " + this.host);
	}

	@Override
	public void run(Date now)
	{
		Moo.mode(Moo.conf.getAntiIdleChannel(), "-b " + this.host);
	}
}