package net.rizon.moo.plugin.logging;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Plugin;

public class logging extends Plugin
{
	private Command ls, sls;
	private Event e;
	
	public logging()
	{
		super("Logging", "Search server logs");
	}

	@Override
	public void start() throws Exception
	{
		ls = new CommandLogSearch(this);
		sls = new CommandSLogSearch(this);
		
		e = new EventLogging();
	}

	@Override
	public void stop()
	{
		ls.remove();
		sls.remove();
		
		e.remove();
	}
}
