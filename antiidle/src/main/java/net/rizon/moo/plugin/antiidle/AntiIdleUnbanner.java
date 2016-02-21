package net.rizon.moo.plugin.antiidle;

import java.util.Date;

import net.rizon.moo.Moo;

class AntiIdleUnbanner implements Runnable
{
	private String host;

	public AntiIdleUnbanner(final String mask)
	{
		String host = mask;
		int a = mask.indexOf('@');
		if (a != -1)
			host = "*!*@" + mask.substring(a + 1);
		this.host = host;

		antiidle.protocol.mode(antiidle.conf.channel, "+b " + this.host);
	}

	@Override
	public void run()
	{
		antiidle.protocol.mode(antiidle.conf.channel, "-b " + this.host);
	}
}