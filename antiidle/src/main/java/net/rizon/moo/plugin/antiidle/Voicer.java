package net.rizon.moo.plugin.antiidle;

import com.google.inject.Inject;
import io.netty.util.concurrent.ScheduledFuture;
import net.rizon.moo.irc.Protocol;

class Voicer implements Runnable
{
	protected final AntiIdleEntry ai;
	protected ScheduledFuture future;

	@Inject
	private Protocol protocol;

	public Voicer(AntiIdleEntry ai)
	{
		this.ai = ai;
	}

	@Override
	public void run()
	{
		antiidle.toBeVoiced.remove(this);
		protocol.write("USERHOST", ai.nick);
	}
}
