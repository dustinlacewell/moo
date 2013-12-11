package net.rizon.moo.grapher.graphs;

import java.util.Date;

import net.rizon.moo.Server;
import net.rizon.moo.grapher.DataSourceType;
import net.rizon.moo.grapher.Graph;
import net.rizon.moo.grapher.RoundRobinArchiveType;

public class ServerUserGraph extends Graph
{
	Server serv;
	
	public ServerUserGraph(Server s)
	{
		super(s.getName() + "-users", 60);
		
		this.serv = s;
		
		this.addDataSource("users", DataSourceType.DST_GAUGE, 120, 0, 200000);
		this.setRRA(RoundRobinArchiveType.RRA_MAX, 1, 525948); // 1 year
	}

	@Override
	public void run(Date now)
	{
		final String[] data = { String.valueOf(serv.users) };
		this.update(data);
	}
}
