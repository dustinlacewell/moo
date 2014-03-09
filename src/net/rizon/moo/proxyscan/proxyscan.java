package net.rizon.moo.proxyscan;

import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

public class proxyscan extends Plugin
{
	private Event e;
	private ScanListener sc;
	protected static final IPCache cache = new IPCache();
	
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
	
	public synchronized static void akill(String ip, int port, String type, boolean input)
	{
		if (cache.isHit(ip))
			return;
		cache.hit(ip);
		
		Moo.privmsg(Moo.conf.getString("proxyscan.channel"), "PROXY FOUND: " + ip + ":" + port + " " + type + " (from input)");
		Moo.akill(ip, "+3d", Moo.conf.getString("proxyscan.ban_message").replace("%i", ip).
				replace("%p", "" + port).replace("%t", type));
	}
}
