package net.rizon.moo.grapher.graphs;

import java.util.Date;

import net.rizon.moo.Server;
import net.rizon.moo.grapher.DataSourceType;
import net.rizon.moo.grapher.Graph;
import net.rizon.moo.grapher.RoundRobinArchiveType;

public class TotalServerGraph extends Graph
{
	public TotalServerGraph()
	{
		super("servers", 60);
		
		this.addDataSource("servers", DataSourceType.DST_GAUGE, 120, 0, 1000);
		this.setRRA(RoundRobinArchiveType.RRA_MAX, 1, 525948); // 1 year
	}

	@Override
	public void run(Date now)
	{
		final String[] data = { String.valueOf(Server.getServers().length) };
		this.update(data);
	}
}
