package net.rizon.moo.grapher;

import java.util.HashMap;

import net.rizon.moo.mpackage;
import net.rizon.moo.server;
import net.rizon.moo.grapher.graphs.totalServerGraph;
import net.rizon.moo.grapher.graphs.totalUserGraph;

public class grapher extends mpackage
{
	public grapher()
	{
		super("Grapher", "Creates graphs");
		
		new eventGraph(this);
		
		new totalServerGraph().start();
		new totalUserGraph().start();
	}
	
	protected HashMap<server, graph> serverGraphs = new HashMap<server, graph>();
}