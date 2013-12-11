package net.rizon.moo.commits;

import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

public class commits extends Plugin
{
	protected static Server s;
	private Event e;
	
	public commits()
	{
		super("Commits", "Manages and shows commits made to repositories");
	}

	@Override
	public void start() throws Exception
	{
		
		s = new Server(Moo.conf.getCommitsIP(), Moo.conf.getCommitsPort());	
		s.start();
		
		e = new EventCommit();
	}

	@Override
	public void stop()
	{
		s.stopServer();
		e.remove();
	}
}