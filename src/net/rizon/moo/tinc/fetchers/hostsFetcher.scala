package net.rizon.moo.tinc.fetchers

import net.rizon.moo.moo
import net.rizon.moo.servercontrol._
import net.rizon.moo.tinc
import net.rizon.moo.tinc._
import net.rizon.moo.servercontrol
import scala.collection.mutable.LinkedList

object hostsFetcher
{
	def create(n: node, con: connection, source: String, target: String): hostsFetcher =
	{
		var cmd = "ls -l " + tinc.tinc.tincBase + "/" + n.getLayer().getName() + "/hosts"
		return new hostsFetcher(n, con, cmd, source, target)
	}
}

class hostsFetcher (n: node, c: connection, commands: String, source: String, target: String) extends process(c, commands)
{
	n.hosts = LinkedList()
	
	private def fetchHosts(h: host)
	{
		var con = connection.findOrCreateConncetion(n.getServer().getServerInfo())
		var p: process = hostFetcher.create(n, con, h, source, target)
		p.start()
	}
	
	override def onLine(in: String)
	{
		var stripped = in
		while (stripped.indexOf("  ") != -1)
			stripped = stripped.replaceAll("  ", " ")
		var tokens: Array[String] = stripped.split(" ")
		if (tokens.length != 9)
			return

		var h = new host()
		h.name = tokens(tokens.length - 1)
		n.hosts = n.hosts :+ h
		
		this.fetchHosts(h)
	}
}
