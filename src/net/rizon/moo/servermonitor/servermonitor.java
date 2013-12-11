package net.rizon.moo.servermonitor;

import java.util.Date;

import net.rizon.moo.Moo;
import net.rizon.moo.MPackage;
import net.rizon.moo.Server;
import net.rizon.moo.Timer;

class requester extends Timer
{
	public requester()
	{
		super(300, true);
	}

	@Override
	public void run(Date now)
	{
		Moo.sock.write("MAP");
		for (Server s : Server.getServers())
			if (s.isServices() == false)
			{
				Moo.sock.write("STATS o " + s.getName());
				Moo.sock.write("STATS c " + s.getName());
			}
		
		new DNSChecker().start();
		
		for (Server s : Server.getServers())
			if (!s.isHub() && !s.isServices())
				new PingChecker(s, 10).start();
		
		CertChecker.run();
	}
}

public class servermonitor extends MPackage
{
	public servermonitor()
	{
		super("Server Monitor", "Monitor servers");
		
		new CommandScheck(this);
		new CommandServer(this);
		new CommandSplit(this);
		new EventSplit();
		
		new requester().start();
	}
}
