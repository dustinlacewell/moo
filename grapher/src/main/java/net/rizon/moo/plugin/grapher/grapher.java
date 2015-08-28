package net.rizon.moo.plugin.grapher;

import io.netty.util.concurrent.ScheduledFuture;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;
import net.rizon.moo.plugin.grapher.conf.GrapherConfiguration;
import net.rizon.moo.plugin.grapher.graphs.ServerUserGraph;
import net.rizon.moo.plugin.grapher.graphs.TotalOlineGraph;
import net.rizon.moo.plugin.grapher.graphs.TotalServerGraph;
import net.rizon.moo.plugin.grapher.graphs.TotalUserGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class grapher extends Plugin
{	
	public static GrapherConfiguration conf;

	protected Map<Server, ServerUserGraph> serverGraphs = new HashMap<Server, ServerUserGraph>();

	private Event e;
	private ScheduledFuture oline, server, user;

	public grapher() throws Exception
	{
		super("Grapher", "Creates graphs");
		conf = GrapherConfiguration.load();
	}


	@Override
	public void start() throws Exception
	{
		e = new EventGraph(this);

		oline = Moo.scheduleAtFixedRate(new TotalOlineGraph(), 1, TimeUnit.MINUTES);
		server = Moo.scheduleAtFixedRate(new TotalServerGraph(), 1, TimeUnit.MINUTES);
		user = Moo.scheduleAtFixedRate(new TotalUserGraph(), 1, TimeUnit.MINUTES);
	}

	@Override
	public void stop()
	{
		for (ServerUserGraph g : serverGraphs.values())
			g.future.cancel(false);

		oline.cancel(false);
		server.cancel(false);
		user.cancel(false);

		e.remove();
	}
}