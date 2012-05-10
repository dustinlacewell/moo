package net.rizon.moo.grapher.graphs;

import java.util.Date;
import java.util.HashSet;

import net.rizon.moo.server;
import net.rizon.moo.grapher.dataSourceType;
import net.rizon.moo.grapher.graph;
import net.rizon.moo.grapher.roundRobinArchiveType;

public class totalOlineGraph extends graph
{
	public totalOlineGraph()
	{
		super("opers", 60);
		
		this.addDataSource("opers", dataSourceType.DST_GAUGE, 120, 0, 1000);
		this.setRRA(roundRobinArchiveType.RRA_MAX, 1, 525948); // 1 year
	}

	@Override
	public void run(Date now)
	{
		HashSet<String> olines = new HashSet<String>();
		for (server s : server.getServers())
			olines.addAll(s.olines);
		
		if (olines.isEmpty() == false)
		{
			final String[] data = { String.valueOf(olines.size()) };
			this.update(data);
		}
	}
}
