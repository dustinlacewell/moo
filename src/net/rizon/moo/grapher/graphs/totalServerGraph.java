package net.rizon.moo.grapher.graphs;

import java.util.Date;

import net.rizon.moo.server;
import net.rizon.moo.grapher.dataSourceType;
import net.rizon.moo.grapher.graph;
import net.rizon.moo.grapher.roundRobinArchiveType;

public class totalServerGraph extends graph
{
	public totalServerGraph()
	{
		super("servers", 60);
		
		this.addDataSource("servers", dataSourceType.DST_GAUGE, 120, 0, 1000);
		this.setRRA(roundRobinArchiveType.RRA_MAX, 1, 525948 * 10); // 10 years
	}

	@Override
	public void run(Date now)
	{
		final String[] data = { String.valueOf(server.getServers().length) };
		this.update(data);
	}
}
