package net.rizon.moo.plugin.commits;

import net.rizon.moo.Event;
import net.rizon.moo.logging.LoggerUtils;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.commits.conf.CommitsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class commits extends Plugin
{
	protected static final Logger logger = LoggerFactory.getLogger(commits.class);

	protected static Server s;
	private Event e;
	public static CommitsConfiguration conf;

	public commits() throws Exception
	{
		super("Commits", "Manages and shows commits made to repositories");
		conf = CommitsConfiguration.load();
	}

	@Override
	public void start() throws Exception
	{
		s = new Server(conf.ip, conf.port);
		LoggerUtils.initThread(logger, s);
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
