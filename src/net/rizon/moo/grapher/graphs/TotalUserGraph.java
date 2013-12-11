package net.rizon.moo.grapher.graphs;

import java.util.Date;

import net.rizon.moo.Server;
import net.rizon.moo.grapher.DataSourceType;
import net.rizon.moo.grapher.Graph;
import net.rizon.moo.grapher.RoundRobinArchiveType;

public class TotalUserGraph extends Graph
{
	public TotalUserGraph()
	{
		super("users", 60);
		
		this.addDataSource("users", DataSourceType.DST_GAUGE, 120, 0, 200000);
		this.setRRA(RoundRobinArchiveType.RRA_MAX, 1, 525948); // 1 year
	}

	@Override
	public void run(Date now)
	{
		final String[] data = { String.valueOf(Server.cur_total_users) };
		this.update(data);
	}
}
