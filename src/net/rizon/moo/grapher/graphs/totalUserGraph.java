package net.rizon.moo.grapher.graphs;

import java.util.Date;

import net.rizon.moo.server;
import net.rizon.moo.grapher.dataSourceType;
import net.rizon.moo.grapher.graph;
import net.rizon.moo.grapher.roundRobinArchiveType;

public class totalUserGraph extends graph
{
	public totalUserGraph()
	{
		super("users", 60);
		
		this.addDataSource("users", dataSourceType.DST_GAUGE, 120, 0, 200000);
		this.setRRA(roundRobinArchiveType.RRA_MAX, 1, 525948 * 10); // 10 years
	}

	@Override
	public void run(Date now)
	{
		final String[] data = { String.valueOf(server.cur_total_users) };
		this.update(data);
	}
}
