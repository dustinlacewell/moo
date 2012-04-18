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
		super(60, true);
	}

	@Override
	public void run(Date now)
	{
		moo.sock.write("MAP");
		for (server s : server.getServers())
			moo.sock.write("STATS c " + s.getName());
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
		
		new mapRequester().start();
	}
}
