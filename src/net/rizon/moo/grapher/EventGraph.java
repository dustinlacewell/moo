package net.rizon.moo.grapher;

import net.rizon.moo.Event;
import net.rizon.moo.Server;
import net.rizon.moo.grapher.graphs.ServerUserGraph;

class EventGraph extends Event
{
	private Grapher pkg;
	
	public EventGraph(Grapher pkg)
	{
		this.pkg = pkg;
	}
	
	@Override
	public void onServerCreate(Server serv)
	{
		graph g = new ServerUserGraph(serv);
		g.start();
		this.pkg.serverGraphs.put(serv, g);
	}
	
	@Override
	public void onServerDestroy(Server serv) 
	{
		this.pkg.serverGraphs.remove(serv).stop();
	}
}