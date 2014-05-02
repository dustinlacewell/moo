package net.rizon.moo.plugin.tinc;

import java.util.ArrayList;
import java.util.LinkedList;

public class Node
{
	private Server server;
	private Layer layer;
	
	/* tinc.conf */
	public String name;
	public String pubAddress;
	public LinkedList<String> connectTo = new LinkedList<String>();
	/* host config files on this machine */
	public LinkedList<Host> hosts = new LinkedList<Host>();
	
	public Node(Server serv, Layer l)
	{
		this.server = serv;
		this.layer = l;
	}
	
	public Server getServer()
	{
		return this.server;
	}
	
	public Layer getLayer()
	{
		return this.layer;
	}
	
	public Host getHost(String name)
	{
		for (Host h : hosts)
			if (h.name.equals(name))
					return h;
		return null;
	}
	
	private static LinkedList<Node> nodes = new LinkedList<Node>();
	
	protected static Node getNode(Server serv, Layer l)
	{
		for (Node n : nodes)
			if (n.getServer() == serv && n.getLayer() == l)
				return n;
		
		Node n = new Node(serv, l);
		nodes.add(n);
		return n;
	}
	
	protected static Node[] getNodes()
	{
		Node[] n = new Node[nodes.size()];
		nodes.toArray(n);
		return n;
	}
	
	public static Node[] getNodesForLayer(Layer l)
	{
		ArrayList<Node> no = new ArrayList<Node>();
		for (Node n : nodes)
			if (n.getLayer() == l)
				no.add(n);
		Node[] n = new Node[no.size()];
		no.toArray(n);
		return n;
	}
	
	public static Node getNodeWithConfigName(Layer l, String name)
	{
		for (Node n : nodes)
			if (n.getLayer() == l && n.name.equals(name))
				return n;
		return null;
	}
}