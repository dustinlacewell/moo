package net.rizon.moo.plugin.grapher;

import net.rizon.moo.Event;
import net.rizon.moo.Server;
import net.rizon.moo.plugin.grapher.graphs.ServerUserGraph;

class EventGraph extends Event
{
	private grapher pkg;
	
	public EventGraph(grapher pkg)
	{
		this.pkg = pkg;
	}
	
	@Override
	public void onServerCreate(Server serv)
	{
		Graph g = new ServerUserGraph(serv);
		g.start();
		this.pkg.serverGraphs.put(serv, g);
	}
	
	@Override
	public void onServerDestroy(Server serv) 
	{
		this.pkg.serverGraphs.remove(serv).stop();
	}
}