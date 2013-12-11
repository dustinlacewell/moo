package net.rizon.moo.grapher;

import java.util.HashMap;

import net.rizon.moo.MPackage;
import net.rizon.moo.Server;
import net.rizon.moo.grapher.graphs.TotalOlineGraph;
import net.rizon.moo.grapher.graphs.TotalServerGraph;
import net.rizon.moo.grapher.graphs.TotalUserGraph;

public class Grapher extends MPackage
{
	public Grapher()
	{
		super("Grapher", "Creates graphs");
		
		new EventGraph(this);
		
		new TotalOlineGraph().start();
		new TotalServerGraph().start();
		new TotalUserGraph().start();
	}
	
	protected HashMap<Server, graph> serverGraphs = new HashMap<Server, graph>();
}