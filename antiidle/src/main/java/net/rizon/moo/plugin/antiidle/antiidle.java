package net.rizon.moo.plugin.antiidle;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Message;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.antiidle.conf.AntiIdleConfiguration;

public class antiidle extends Plugin
{
	private Command idle;
	private Event e;
	private Message m;
	public static AntiIdleConfiguration conf;

	public antiidle() throws Exception
	{
		super("Anti idle", "Prevents users from idling in channels");
		conf = AntiIdleConfiguration.load();
	}

	@Override
	public void start() throws Exception
	{
		idle = new CommandIdle(this);
		e = new EventAntiidle();
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