package net.rizon.moo.plugin.grapher.graphs;

import java.util.HashSet;

import net.rizon.moo.Server;
import net.rizon.moo.plugin.grapher.DataSourceType;
import net.rizon.moo.plugin.grapher.Graph;
import net.rizon.moo.plugin.grapher.RoundRobinArchiveType;

public class TotalOlineGraph extends Graph
{
	public TotalOlineGraph()
	{
		super("opers");

		this.addDataSource("opers", DataSourceType.DST_GAUGE, 120, 0, 1000);
		this.setRRA(RoundRobinArchiveType.RRA_MAX, 1, 525948); // 1 year
	}

	@Override
	public void run()
	{
		HashSet<String> olines = new HashSet<String>();
		for (Server s : Server.getServers())
			if (s.olines != null)
				olines.addAll(s.olines.keySet());

		if (olines.isEmpty() == false)
		{
			final String[] data = { String.valueOf(olines.size()) };
			this.update(data);
		}
	}
}
