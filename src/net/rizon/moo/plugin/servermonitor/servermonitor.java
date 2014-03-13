package net.rizon.moo.plugin.servermonitor;

import java.util.Date;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
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
		
		CertChecker.run();
	}
}

public class servermonitor extends Plugin
{
	private Command scheck;
	private CommandServer server;
	private Command split;
	private Event e;
	private Timer r;
	
	public servermonitor()
	{
		super("Server Monitor", "Monitor servers");
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
