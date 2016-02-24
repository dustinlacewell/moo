package net.rizon.moo.plugin.servermonitor;

import com.google.inject.Inject;
import net.rizon.moo.Moo;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;
import net.rizon.moo.plugin.servermonitor.scheck.SCheck;

class CertChecker implements Runnable
{
	private static final int sslPort = 6697;
	private static int lastIdx = -1;
	
	@Inject
	private ServerManager serverManager;
	
	@Inject
	private Config conf;

	@Override
	public void run()
	{
		Server[] servers = serverManager.getServers();
		if (servers.length == 0)
			return;

		lastIdx += 1;
		if (lastIdx >= servers.length)
			lastIdx = 0;

		Server s = servers[lastIdx];

		if (!s.isHub() && !s.isServices())
		{
			SCheck sc = new SCheck(s, conf.moo_log_channels, true, CertChecker.sslPort, true, false);
			Moo.injector.injectMembers(sc);
			sc.start();
		}
	}
}