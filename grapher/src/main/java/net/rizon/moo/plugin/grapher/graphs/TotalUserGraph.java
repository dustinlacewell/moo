package net.rizon.moo.plugin.grapher.graphs;

import com.google.inject.Inject;
import net.rizon.moo.irc.ServerManager;
import net.rizon.moo.plugin.grapher.DataSourceType;
import net.rizon.moo.plugin.grapher.Graph;
import net.rizon.moo.plugin.grapher.RoundRobinArchiveType;
import net.rizon.moo.plugin.grapher.conf.GrapherConfiguration;

public class TotalUserGraph extends Graph
{
	@Inject
	private ServerManager serverManager;
	
	@Inject
	public TotalUserGraph(GrapherConfiguration config)
	{
		super(config, "users");

		this.addDataSource("users", DataSourceType.DST_GAUGE, 120, 0, 200000);
		this.setRRA(RoundRobinArchiveType.RRA_MAX, 1, 525948); // 1 year
	}

	@Override
	public void run()
	{
		final String[] data = { String.valueOf(serverManager.cur_total_users) };
		this.update(data);
	}
}
