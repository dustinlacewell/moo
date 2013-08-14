package net.rizon.moo.tinc;

import java.util.ArrayList;
import java.util.LinkedList;

public class node
{
	private server server;
	private layer layer;
	
	/* tinc.conf */
	public String name;
	public String pubAddress;
	public LinkedList<String> connectTo = new LinkedList<String>();
	/* host config files on this machine */
	public LinkedList<host> hosts = new LinkedList<host>();
	
	public node(server serv, layer l)
	{
		this.server = serv;
		this.layer = l;
	}
	
	public server getServer()
	{
		return this.server;
	}
	
	public layer getLayer()
	{
		return this.layer;
	}
	
	public host getHost(String name)
	{
		for (host h : hosts)
			if (h.name.equals(name))
					return h;
		return null;
	}
	
	private static LinkedList<node> nodes = new LinkedList<node>();
	
	protected static node getNode(server serv, layer l)
	{
		for (node n : nodes)
			if (n.getServer() == serv && n.getLayer() == l)
				return n;
		
		node n = new node(serv, l);
		nodes.add(n);
		return n;
	}
	
	protected static node[] getNodes()
	{
		node[] n = new node[nodes.size()];
		nodes.toArray(n);
		return n;
	}
	
	public static node[] getNodesForLayer(layer l)
	{
		ArrayList<node> no = new ArrayList<node>();
		for (node n : nodes)
			if (n.getLayer() == l)
				no.add(n);
		node[] n = new node[no.size()];
		no.toArray(n);
		return n;
	}
	
	public static node getNodeWithConfigName(layer l, String name)
	{
		for (node n : nodes)
			if (n.getLayer() == l && n.name.equals(name))
				return n;
		return null;
	}
}