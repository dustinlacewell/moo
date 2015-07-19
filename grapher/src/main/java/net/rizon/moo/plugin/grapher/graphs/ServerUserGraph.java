package net.rizon.moo.plugin.grapher.graphs;

import io.netty.util.concurrent.ScheduledFuture;
import java.util.Date;

import net.rizon.moo.Server;
import net.rizon.moo.plugin.grapher.DataSourceType;
import net.rizon.moo.plugin.grapher.Graph;
import net.rizon.moo.plugin.grapher.RoundRobinArchiveType;

public class ServerUserGraph extends Graph
{
	Server serv;
	public ScheduledFuture future;

	public ServerUserGraph(Server s)
	{
		super(s.getName() + "-users");

		this.serv = s;

		this.addDataSource("users", DataSourceType.DST_GAUGE, 120, 0, 200000);
		this.setRRA(RoundRobinArchiveType.RRA_MAX, 1, 525948); // 1 year
	}

	@Override
	public void run()
	{
		final String[] data = { String.valueOf(serv.users) };
		this.update(data);
	}
}
