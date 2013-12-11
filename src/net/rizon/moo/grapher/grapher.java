package net.rizon.moo.grapher;

import java.util.HashMap;

import net.rizon.moo.Event;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;
import net.rizon.moo.grapher.graphs.TotalOlineGraph;
import net.rizon.moo.grapher.graphs.TotalServerGraph;
import net.rizon.moo.grapher.graphs.TotalUserGraph;

public class grapher extends Plugin
{
	protected HashMap<Server, Graph> serverGraphs = new HashMap<Server, Graph>();
	
	private Event e;
	private Graph oline, server, user;
	
	public grapher()
	{
		super("Grapher", "Creates graphs");
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