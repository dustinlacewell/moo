package net.rizon.moo.tinc.fetchers

import net.rizon.moo.moo
import net.rizon.moo.tinc
import net.rizon.moo.tinc._
import net.rizon.moo.servercontrol._

import scala.collection.mutable.LinkedList

object hostFetcher
{
	def create(n: node, con: connection, h: host, source: String, target: String): hostFetcher =
	{
		var cmd = "cat " + tinc.tinc.tincBase + "/" + n.getLayer().getName() + "/hosts/" + h.name
		return new hostFetcher(n, con, cmd, h, source, target)
	}
}

class hostFetcher (n: node, c: connection, commands: String, h: host, source: String, target: String) extends process(c, commands)
{
	var inKey = false
	
	override def onLine(in: String)
	{
		var stripped = in
		while (stripped.indexOf("  ") != -1)
			stripped = stripped.replaceAll("  ", " ")

		var tokens = stripped.split(" ")
		
		if (tokens(0).equals("Address"))
			h.address = tokens(2)
		else if (tokens(0).equals("Port"))
			h.port = Integer.parseInt(tokens(2))
		else if (tokens(0).equals("Subnet"))
			h.subnets = h.subnets :+ tokens(2)
		else if (tokens(0).equals("-----BEGIN RSA PUBLIC KEY-----"))
			inKey = true
		else if (tokens(0).equals("-----END RSA PUBLIC KEY-----"))
			inKey = false
		else if (inKey)
			h.key += in
	}
}