package net.rizon.moo.plugin.grapher;

import io.netty.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Server;
import net.rizon.moo.plugin.grapher.conf.GrapherConfiguration;
import net.rizon.moo.plugin.grapher.graphs.ServerUserGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EventGraph extends Event
{
	private static final Logger logger = LoggerFactory.getLogger(EventGraph.class);
	
	private grapher pkg;

	public EventGraph(grapher pkg)
	{
		this.pkg = pkg;
	}

	@Override
	public void onServerCreate(Server serv)
	{
		ServerUserGraph g = new ServerUserGraph(serv);
		ScheduledFuture future = Moo.scheduleAtFixedRate(g, 1, TimeUnit.MINUTES);
		g.future = future;
		this.pkg.serverGraphs.put(serv, g);
	}

	@Override
	public void onServerDestroy(Server serv)
	{
		this.pkg.serverGraphs.remove(serv).future.cancel(false);
	}

	@Override
	public void onReload(CommandSource source)
	{
		try
		{
			grapher.conf = GrapherConfiguration.load();
		}
		catch (Exception ex)
		{
			source.reply("Error reloading grapher configuration: " + ex.getMessage());
			
			logger.warn("Unable to reload configuration", ex);
		}
	}
}