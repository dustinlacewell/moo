package net.rizon.moo.plugin.commits;

import net.rizon.moo.Event;
import net.rizon.moo.Logger;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.commits.conf.CommitsConfiguration;

public class commits extends Plugin
{
	protected static Server s;
	private Event e;
	public static CommitsConfiguration conf;
	protected static final Logger log = Logger.getLogger(commits.class.getName());

	public commits() throws Exception
	{
		super("Commits", "Manages and shows commits made to repositories");
		conf = CommitsConfiguration.load();
	}

	@Override
	public void start() throws Exception
	{
		s = new Server(conf.ip, conf.port);
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
