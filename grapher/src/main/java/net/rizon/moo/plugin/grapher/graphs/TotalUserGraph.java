package net.rizon.moo.plugin.grapher.graphs;

import net.rizon.moo.Server;
import net.rizon.moo.plugin.grapher.DataSourceType;
import net.rizon.moo.plugin.grapher.Graph;
import net.rizon.moo.plugin.grapher.RoundRobinArchiveType;

public class TotalUserGraph extends Graph
{
	public TotalUserGraph()
	{
		super("users");

		this.addDataSource("users", DataSourceType.DST_GAUGE, 120, 0, 200000);
		this.setRRA(RoundRobinArchiveType.RRA_MAX, 1, 525948); // 1 year
	}

	@Override
	public void run()
	{
		final String[] data = { String.valueOf(Server.cur_total_users) };
		this.update(data);
	}
}
