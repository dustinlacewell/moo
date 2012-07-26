package net.rizon.moo.grapher;

import net.rizon.moo.event;
import net.rizon.moo.server;
import net.rizon.moo.grapher.graphs.serverUserGraph;

class eventGraph extends event
{
	private grapher pkg;
	
	public eventGraph(grapher pkg)
	{
		this.pkg = pkg;
	}
	
	@Override
	public void onServerCreate(server serv)
	{
		graph g = new serverUserGraph(serv);
		g.start();
		this.pkg.serverGraphs.put(serv, g);
	}
	
	@Override
	public void onServerDestroy(server serv) 
	{
		this.pkg.serverGraphs.remove(serv).stop();
	}
}