package net.rizon.moo.plugin.commands.time;

import com.google.inject.Inject;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;

public class CheckTimesTimer implements Runnable
{
	@Inject
	private ServerManager serverManager;

	@Inject
	private Protocol protocol;

	@Override
	public void run()
	{
		Message391.known_times.clear();
		Message391.hourly_check = true;

		for (Server s : serverManager.getServers())
		{
			if (s.isServices())
				continue;
			protocol.write("TIME", s.getName());
			Message391.waiting_for.add(s.getName());
		}
	}
}