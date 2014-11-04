package net.rizon.moo.plugin.fun;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.conf.Protocol;

public class fun extends Plugin
{
	private Command rt;
	private Event e;
	
	public fun() throws Exception
	{
		super("FUN", "Provides fun features");
	}

	@Override
	public void start() throws Exception
	{
		if (Moo.conf.general.protocol == Protocol.PLEXUS)
		{
			rt = new CommandRizonTime(this);
			e = new EventFun();
		}
	}

	@Override
	public void stop()
	{
		if (rt != null)
			rt.remove();
		if (e != null)
			e.remove();
	}
}