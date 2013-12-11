package net.rizon.moo.antiidle;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Message;
import net.rizon.moo.Plugin;

public class antiidle extends Plugin
{
	private Command idle;
	private Event e;
	private Message m;
	
	public antiidle()
	{
		super("Anti idle", "Prevents users from idling in channels");
	}

	@Override
	public void start() throws Exception
	{
		idle = new CommandIdle(this);
		e = new eventAntiIdle();
		m = new MessageUserhost();
	}

	@Override
	public void stop()
	{
		idle.remove();
		e.remove();
		m.remove();
	}
}