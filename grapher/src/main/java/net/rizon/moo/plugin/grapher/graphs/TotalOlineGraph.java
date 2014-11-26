package net.rizon.moo.plugin.grapher.graphs;

import java.util.Date;
import java.util.HashSet;

import net.rizon.moo.Server;
import net.rizon.moo.plugin.grapher.DataSourceType;
import net.rizon.moo.plugin.grapher.Graph;
import net.rizon.moo.plugin.grapher.RoundRobinArchiveType;

public class TotalOlineGraph extends Graph
{
	public TotalOlineGraph()
	{
		super("opers", 60);

		this.addDataSource("opers", DataSourceType.DST_GAUGE, 120, 0, 1000);
		this.setRRA(RoundRobinArchiveType.RRA_MAX, 1, 525948); // 1 year
	}

	@Override
	public void run(Date now)
	{
		HashSet<String> olines = new HashSet<String>();
		for (Server s : Server.getServers())
			olines.addAll(s.olines.keySet());

		if (olines.isEmpty() == false)
		{
			final String[] data = { String.valueOf(olines.size()) };
			this.update(data);
		}
	}
}
