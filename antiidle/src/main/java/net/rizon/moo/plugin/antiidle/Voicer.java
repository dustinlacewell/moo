package net.rizon.moo.plugin.antiidle;

import io.netty.util.concurrent.ScheduledFuture;
import net.rizon.moo.Moo;

class Voicer implements Runnable
{
	protected final AntiIdleEntry ai;
	protected ScheduledFuture future;

	public Voicer(AntiIdleEntry ai)
	{
		this.ai = ai;
	}

	@Override
	public void run()
	{
		EventAntiidle.toBeVoiced.remove(this);
		Moo.write("USERHOST", ai.nick);
	}
}
