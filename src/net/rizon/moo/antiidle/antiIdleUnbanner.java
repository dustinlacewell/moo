package net.rizon.moo.antiidle;

import java.util.Date;

import net.rizon.moo.moo;
import net.rizon.moo.timer;

class antiIdleUnbanner extends timer
{
	private String host;
	
	public antiIdleUnbanner(final String mask)
	{
		super(moo.conf.getAntiIdleBanTime() * 60, false);
	
		String host = mask;
		int a = mask.indexOf('@');
		if (a != -1)
			host = "*!*@" + mask.substring(a + 1);
		this.host = host;
		
		moo.mode(moo.conf.getAntiIdleChannel(), "+b " + this.host);
	}

	@Override
	public void run(Date now)
	{
		moo.mode(moo.conf.getAntiIdleChannel(), "-b " + this.host);
	}
}