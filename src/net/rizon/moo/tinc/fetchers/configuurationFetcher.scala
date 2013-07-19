package net.rizon.moo.tinc.fetchers

import net.rizon.moo.moo
import net.rizon.moo.servercontrol._
import net.rizon.moo.tinc
import net.rizon.moo.tinc._
import scala.collection.mutable.LinkedList

object configurationFetcher
{
	def create(n: node, con: connection, source: String, target: String): configurationFetcher =
	{
		var cmd = "cat " + tinc.tinc.tincBase + "/" + n.getLayer().getName() + "/tinc.conf"
		return new configurationFetcher(n, con, cmd, source, target)
	}
	
	var warned = ""
}

class configurationFetcher (n: node, c: connection, commands: String, source: String, target: String) extends process(c, commands)
{
	n.name = ""
	n.pubAddress = ""
	n.connectTo = LinkedList()
	
	override def onLine(in: String)
	{
		var stripped = in
		while (stripped.indexOf("  ") != -1)
			stripped = stripped.replaceAll("  ", " ")

		var tokens = stripped.split(" ")
		
		if (tokens(0).equals("Name"))
			n.name = tokens(1)
		else if (tokens(0).equals("BindToAddress"))
			n.pubAddress = tokens(1)
		else if (tokens(0).equals("ConnectTo"))
			n.connectTo = n.connectTo :+ tokens(1)
	}
	
	override def onFinish()
	{
		if (n.name.isEmpty())
			return
		
		moo.reply(source, target, "[" + this.con.getServerInfo().name + "] Processed tinc configuration for " + n.name + " on layer " + n.getLayer().getName()) 
	}
	
	override def onError(ex: Exception)
	{
		if (configurationFetcher.warned.equals(this.con.getServerInfo().name))
			return
		
		configurationFetcher.warned = this.con.getServerInfo().name
		moo.reply(source, target, "[" + this.con.getServerInfo().name + "] Error: " + ex.getMessage())
	}
}
