package net.rizon.moo.plugin.grapher;

import java.util.HashMap;

import net.rizon.moo.Event;
import net.rizon.moo.Logger;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;
import net.rizon.moo.plugin.grapher.conf.GrapherConfiguration;
import net.rizon.moo.plugin.grapher.graphs.TotalOlineGraph;
import net.rizon.moo.plugin.grapher.graphs.TotalServerGraph;
import net.rizon.moo.plugin.grapher.graphs.TotalUserGraph;

public class grapher extends Plugin
{
	public static GrapherConfiguration conf;

	protected HashMap<Server, Graph> serverGraphs = new HashMap<Server, Graph>();
	protected static final Logger log = Logger.getLogger(grapher.class.getName());

	private Event e;
	private Graph oline, server, user;

	public grapher() throws Exception
	{
		super("Grapher", "Creates graphs");
		conf = GrapherConfiguration.load();
	}


	@Override
	public void start() throws Exception
	{
		e = new EventGraph(this);

		oline = new TotalOlineGraph();
		server = new TotalServerGraph();
		user = new TotalUserGraph();

		oline.start();
		server.start();
		user.start();
	}

	@Override
	public void stop()
	{
		for (Graph g : serverGraphs.values())
			g.stop();

		oline.stop();
		server.stop();
		user.stop();

		e.remove();
	}
}