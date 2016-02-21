package net.rizon.moo.plugin.grapher.graphs;

import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;

import net.rizon.moo.plugin.grapher.DataSourceType;
import net.rizon.moo.plugin.grapher.Graph;
import net.rizon.moo.plugin.grapher.RoundRobinArchiveType;
import net.rizon.moo.plugin.grapher.conf.GrapherConfiguration;

public class TotalOlineGraph extends Graph
{
	@Inject
	private ServerManager serverManager;

	@Inject
	public TotalOlineGraph(GrapherConfiguration config)
	{
		super(config, "opers");

		this.addDataSource("opers", DataSourceType.DST_GAUGE, 120, 0, 1000);
		this.setRRA(RoundRobinArchiveType.RRA_MAX, 1, 525948); // 1 year
	}

	@Override
	public void run()
	{
		Set<String> olines = new HashSet<>();
		for (Server s : serverManager.getServers())
			if (s.olines != null)
				olines.addAll(s.olines.keySet());

		if (olines.isEmpty() == false)
		{
			final String[] data = { String.valueOf(olines.size()) };
			this.update(data);
		}
	}
}
