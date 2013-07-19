package net.rizon.moo.tinc

import scala.collection.mutable.LinkedList

object node
{
	private var nodes: LinkedList[node] = LinkedList()
	
	def getNode(serv: server, l: layer): node =
	{
		for (n <- nodes)
			if (n.getServer() == serv && n.getLayer == l)
				return n

		var n = new node(serv, l)
		nodes = nodes :+ n
		return n
	}
	
	def getNodes() = nodes
	
	def getNodesForLayer(l: layer): LinkedList[node] =
	{
		var nfl: LinkedList[node] = LinkedList()
		for (n <- nodes)
			if (n.getLayer() == l)
				nfl = nfl :+ n
		return nfl
	}
	
	def getNodeWithConfigName(l: layer, name: String): node =
	{
		for (n <- nodes)
			if (n.getLayer() == l && n.name.equals(name))
				return n
		return null
	}
}

class node (server: server, layer: layer)
{
	/* tinc.conf */
	var name = ""
	var pubAddress = ""
	/* connectTo in tinc.conf */
	var connectTo: LinkedList[String] = LinkedList()
	
	/* host config files on the machine */
	var hosts: LinkedList[host] = LinkedList()
	
	def getServer() = server
	def getLayer() = layer
	
	def getHost(n: String): host =
	{
		for (h <- hosts)
			if (h.name.equals(n))
				return h
		return null
	}
}