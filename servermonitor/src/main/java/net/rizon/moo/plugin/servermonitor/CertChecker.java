package net.rizon.moo.plugin.servermonitor;

import net.rizon.moo.Moo;
import net.rizon.moo.Server;

class CertChecker
{
	private static final int sslPort = 6697;
	private static int lastIdx = -1;

	protected static void run()
	{
		Server[] servers = Server.getServers();
		if (servers.length == 0)
			return;

		lastIdx += 1;
		if (lastIdx >= servers.length)
			lastIdx = 0;

		Server s = servers[lastIdx];

		if (!s.isHub() && !s.isServices())
		{
			SCheck sc = new SCheck(s, Moo.conf.moo_log_channels, true, CertChecker.sslPort, true, false);
			sc.start();
		}
	}
}