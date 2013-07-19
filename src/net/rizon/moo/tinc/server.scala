package net.rizon.moo.tinc

import net.rizon.moo.servercontrol._
import scala.collection.mutable.LinkedList

object server
{
	var servers: LinkedList[server] = LinkedList()
	
	def findOrCreateServer(si: serverInfo): server =
	{
		for (s <- servers)
			if (s.getName().equalsIgnoreCase(si.name))
				return s
				
		var s: server = new server(si)
		servers = servers :+ s
		return s
	}
}

class server private (si: serverInfo)
{
	def getServerInfo() = si
	def getName() = si.name
}