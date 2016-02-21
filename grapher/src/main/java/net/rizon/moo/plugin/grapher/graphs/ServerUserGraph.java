package net.rizon.moo.plugin.grapher.graphs;

import io.netty.util.concurrent.ScheduledFuture;
import net.rizon.moo.irc.Server;

import net.rizon.moo.plugin.grapher.DataSourceType;
import net.rizon.moo.plugin.grapher.Graph;
import net.rizon.moo.plugin.grapher.RoundRobinArchiveType;
import net.rizon.moo.plugin.grapher.conf.GrapherConfiguration;

public class ServerUserGraph extends Graph
{
	private Server serv;
	public ScheduledFuture future;

	public ServerUserGraph(GrapherConfiguration config, Server s)
	{
		super(config, s.getName() + "-users");

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
