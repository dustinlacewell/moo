package net.rizon.moo.plugin.servermonitor;

import io.netty.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.servermonitor.conf.ServerMonitorConfiguration;

class Requester implements Runnable
{
	@Override
	public void run()
	{
		new DNSChecker().start();

		CertChecker.run();
	}
}

public class servermonitor extends Plugin
{
	private Command scheck;
	private CommandServer server;
	private Command split;
	private Event e;
	private ScheduledFuture requester;
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

		requester = Moo.scheduleWithFixedDelay(new Requester(), 5, TimeUnit.MINUTES);
	}

	@Override
	public void stop()
	{
		scheck.remove();
		server.remove();
		split.remove();

		e.remove();

		requester.cancel(false);
	}
}
