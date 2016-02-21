package net.rizon.moo.plugin.grapher.graphs;

import com.google.inject.Inject;
import net.rizon.moo.irc.ServerManager;
import net.rizon.moo.plugin.grapher.DataSourceType;
import net.rizon.moo.plugin.grapher.Graph;
import net.rizon.moo.plugin.grapher.RoundRobinArchiveType;
import net.rizon.moo.plugin.grapher.conf.GrapherConfiguration;

public class TotalServerGraph extends Graph
{
	@Inject
	private ServerManager serverManager;
	
	@Inject
	public TotalServerGraph(GrapherConfiguration config)
	{
		super(config, "servers");

		this.addDataSource("servers", DataSourceType.DST_GAUGE, 120, 0, 1000);
		this.setRRA(RoundRobinArchiveType.RRA_MAX, 1, 525948); // 1 year
	}

	@Override
	public void run()
	{
		final String[] data = { String.valueOf(serverManager.getServers().length) };
		this.update(data);
	}
}
