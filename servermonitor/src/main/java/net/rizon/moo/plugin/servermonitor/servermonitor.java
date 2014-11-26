package net.rizon.moo.plugin.servermonitor;

import java.util.Date;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Logger;
import net.rizon.moo.Plugin;
import net.rizon.moo.Timer;
import net.rizon.moo.plugin.servermonitor.conf.ServerMonitorConfiguration;

class requester extends Timer
{
	public requester()
	{
		super(300, true);
	}

	@Override
	public void run(Date now)
	{
		new DNSChecker().start();

		CertChecker.run();
	}
}

public class servermonitor extends Plugin
{
	protected static final Logger log = Logger.getLogger(servermonitor.class.getName());

	private Command scheck;
	private CommandServer server;
	private Command split;
	private Event e;
	private Timer r;
	public static ServerMonitorConfiguration conf;

	public servermonitor() throws Exception
	{
		super("Server Monitor", "Monitor servers");
		conf = ServerMonitorConfiguration.load();
	}

	@Override
	public void start() throws Exception
	{
		scheck = new CommandScheck(this);
		server = new CommandServer(this);
		split = new CommandSplit(this);

		e = new EventSplit();

		r = new requester();

		r.start();
	}

	@Override
	public void stop()
	{
		scheck.remove();
		server.remove();
		split.remove();

		e.remove();

		r.stop();
	}
}
