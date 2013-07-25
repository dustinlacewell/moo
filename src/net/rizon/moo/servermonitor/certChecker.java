package net.rizon.moo.servermonitor;

import net.rizon.moo.moo;
import net.rizon.moo.server;

class certChecker
{
	private static final int sslPort = 6697;
	private static int lastIdx = -1;
	
	protected static void run()
	{
		server[] servers = server.getServers();
		if (servers.length == 0)
			return;

		lastIdx += 1;
		if (lastIdx >= servers.length)
			lastIdx = 0;
			
		server s = servers[lastIdx];
			
		if (!s.isHub() && !s.isServices())
		{
			scheck sc = new scheck(s, moo.conf.getAdminChannels(), true, certChecker.sslPort, true, false);
			sc.start();
		}
	}
}