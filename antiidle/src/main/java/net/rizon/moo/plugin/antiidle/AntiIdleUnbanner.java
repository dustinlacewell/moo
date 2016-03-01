package net.rizon.moo.plugin.antiidle;

import java.util.Date;

import net.rizon.moo.Moo;
import com.google.inject.Inject;
import net.rizon.moo.irc.Protocol;

class AntiIdleUnbanner implements Runnable
{
	private String host;
	@Inject
	private Protocol protocol;

	public AntiIdleUnbanner(final String mask)
	{
		String host = mask;
		int a = mask.indexOf('@');
		if (a != -1)
			host = "*!*@" + mask.substring(a + 1);
		this.host = host;
	}

	public void init()
	{
		protocol.mode(antiidle.conf.channel, "+b " + this.host);
	}

	@Override
	public void run()
	{
		protocol.mode(antiidle.conf.channel, "-b " + this.host);
	}
}