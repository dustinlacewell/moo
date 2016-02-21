package net.rizon.moo.plugin.grapher;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.Arrays;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.rizon.moo.Command;

import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.events.OnServerCreate;
import net.rizon.moo.events.OnServerDestroy;
import net.rizon.moo.irc.Server;
import net.rizon.moo.plugin.grapher.conf.GrapherConfiguration;
import net.rizon.moo.plugin.grapher.graphs.ServerUserGraph;
import net.rizon.moo.plugin.grapher.graphs.TotalOlineGraph;
import net.rizon.moo.plugin.grapher.graphs.TotalServerGraph;
import net.rizon.moo.plugin.grapher.graphs.TotalUserGraph;
import org.slf4j.Logger;

public class grapher extends Plugin implements EventListener
{
	@Inject
	private static Logger logger;

	@Inject
	private GrapherConfiguration conf;

	@Inject
	private TotalOlineGraph totalOlineGraph;

	@Inject
	private TotalServerGraph totalServerGraph;

	@Inject
	private TotalUserGraph totalUserGraph;

	protected Map<Server, ServerUserGraph> serverGraphs = new HashMap<>();

	private ScheduledFuture oline, server, user;

	public grapher() throws Exception
	{
		super("Grapher", "Creates graphs");
		conf = GrapherConfiguration.load();
	}


	@Override
	public void start() throws Exception
	{
		oline = Moo.scheduleAtFixedRate(totalOlineGraph, 1, TimeUnit.MINUTES);
		server = Moo.scheduleAtFixedRate(totalServerGraph, 1, TimeUnit.MINUTES);
		user = Moo.scheduleAtFixedRate(totalUserGraph, 1, TimeUnit.MINUTES);
	}

	@Override
	public void stop()
	{
		for (ServerUserGraph g : serverGraphs.values())
			g.future.cancel(false);

		oline.cancel(false);
		server.cancel(false);
		user.cancel(false);
	}
	
	@Subscribe
	public void onServerCreate(OnServerCreate evt)
	{
		Server serv = evt.getServer();
		
		ServerUserGraph g = new ServerUserGraph(conf, serv);
		ScheduledFuture future = Moo.scheduleAtFixedRate(g, 1, TimeUnit.MINUTES);
		g.future = future;
		
		serverGraphs.put(serv, g);
	}

	@Subscribe
	public void onServerDestroy(OnServerDestroy evt)
	{
		Server serv = evt.getServer();
		
		serverGraphs.remove(serv).future.cancel(false);
	}

	@Subscribe
	public void onReload(OnReload evt)
	{
		try
		{
			conf = GrapherConfiguration.load();
		}
		catch (Exception ex)
		{
			evt.getSource().reply("Error reloading grapher configuration: " + ex.getMessage());
			
			logger.warn("Unable to reload configuration", ex);
		}
	}

	@Override
	public List<Command> getCommands()
	{
		return Arrays.asList();
	}

	@Override
	protected void configure()
	{
		bind(grapher.class).toInstance(this);

		bind(GrapherConfiguration.class).toInstance(conf);

		bind(TotalOlineGraph.class);
		bind(TotalServerGraph.class);
		bind(TotalUserGraph.class);
	}
}