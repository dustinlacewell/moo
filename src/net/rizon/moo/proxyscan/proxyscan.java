package net.rizon.moo.proxyscan;

import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

public class proxyscan extends Plugin
{
	private Event e;
	private ScanListener sc;
	public static final String check_string = "Orphean Beholder Scary Doubt";
	
	public proxyscan()
	{
		super("Proxyscan", "Checks connecting users for proxies");
	}

	@Override
	public void start() throws Exception
	{
		e = new EventProxyScan();
		sc = new ScanListener(Moo.conf.getString("proxyscan.listenip"), Moo.conf.getInt("proxyscan.listenport"));
		sc.start();
	}

	@Override
	public void stop()
	{
		e.remove();
		sc.shutdown();
	}
}
