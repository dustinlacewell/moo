package net.rizon.moo.servermonitor;

import java.util.Date;

import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;
import net.rizon.moo.timer;

class mapRequester extends timer
{
	public mapRequester()
	{
		super(300, true);
	}

	@Override
	public void run(Date now)
	{
		moo.sock.write("MAP");
		for (server s : server.getServers())
			if (s.isServices() == false)
			{
				moo.sock.write("STATS o " + s.getName());
				moo.sock.write("STATS c " + s.getName());
			}
		
		new dnsChecker().start();
		
		for (server s : server.getServers())
			if (!s.isHub() && !s.isServices())
				new pingChecker(s, 20).start();
	}
}

public class servermonitor extends mpackage
{
	public servermonitor()
	{
		super("Server Monitor", "Monitor servers");
		
		new commandServer(this);
		new commandSplit(this);
		new eventSplit();
		new messageWallops();
		
		new mapRequester().start();
	}
}
