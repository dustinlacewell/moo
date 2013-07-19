package net.rizon.moo.tinc

import net.rizon.moo._
import net.rizon.moo.servercontrol
import net.rizon.moo.servercontrol._
import net.rizon.moo.tinc.fetchers._

import scala.collection.mutable.LinkedList

class commandTinc (pkg: mpackage) extends command(pkg, ".TINC", "Manages tinc configurations")
{
	this.requiresChannel(moo.conf.getAdminChannels())
	
	private def fetchConfiguration(n: node, source: String, target: String)
	{
		var con = connection.findOrCreateConncetion(n.getServer().getServerInfo())
		var p: process = configurationFetcher.create(n, con, source, target)
		p.start()
	}
	
	private def fetchHosts(n: node, source: String, target: String)
	{
		var con = connection.findOrCreateConncetion(n.getServer().getServerInfo())
		var p: process = hostsFetcher.create(n, con, source, target)
		p.start()
	}
	
	override def execute(source: String, target: String, params: Array[String])
	{
		if (params.size > 1 && params(1).equalsIgnoreCase("HELP"))
		{
			moo.reply(source, target, ".tinc - view overview")
			moo.reply(source, target, ".tinc fetch server.name - update local copy of tinc configuration for the given server")
		}
		else if (params.size > 2 && params(1).equalsIgnoreCase("FETCH"))
		{
			var server_name = params(2)
			
			var server_info: Array[servercontrol.serverInfo] = servercontrol.servercontrol.findServers(server_name, "ssh")
			if (server_info == null)
			{
				moo.reply(source, target, "No servers found for " + server_name)
				return
			}
			
			for (si <- server_info)
			{
				var serv: server = server.findOrCreateServer(si)
									
				moo.reply(source, target, "Fetching configuration for " + serv.getName() + "...")
				
				for (l: layer <- tinc.layers)
				{
					var n: node = node.getNode(serv, l)
					
					fetchConfiguration(n, source, target)
					fetchHosts(n, source, target)
				}
			}
		}
		else
		{
			var shown = false
			
			for (layer <- tinc.layers)
			{
				var shown_layer = false
				
				var nodes: LinkedList[node] = node.getNodesForLayer(layer)
				for (n <- nodes)
				{
					if (!n.name.isEmpty())
					{
						var links = ""
						for (link <- n.connectTo)
							links += link + " "
						links = links.trim
						links = links.replaceAll(" ", ", ")
						
						if (!shown_layer)
						{
							moo.reply(source, target, "Layer: " + layer.getName())
							shown_layer = true
						}
						
						moo.reply(source, target, " Server: " + n.getServer.getName() + " Node: " + n.name + " Links: " + links)
						shown = true
						
						for (link <- n.connectTo)
						{
							var found = false
							
							for (host <- n.hosts)
							{
								if (host.name.equals(link))
								{
									found = true
								}
							}
							
							if (!found)
							{
								moo.reply(source, target, "  " + n.name + " has ConnectTo for " + link + " but no host file")
							}
						}
						
						for (host <- n.hosts)
						{
							var found = false
							
							for (link <- n.connectTo)
							{
								if (host.name.equals(link))
								{
									found = true
								}
							}
							
							if (!found)
							{
								moo.reply(source, target, "  " + n.name + " has host file for " + host.name + " but no ConnectTo")
							}
							
							if (!host.hasKnownHost(n))
							{
								moo.reply(source, target, "  " + n.name + " has host file for unknown host " + host.name)
							}
							else if (!host.hasSelfConfiguration(n))
							{
								moo.reply(source, target, "  " + n.name + " has no self configuration")
							}
							else if (!host.isUpToDate(n))
							{
								moo.reply(source, target, "  " + n.name + " has out of date host file for " + host.name)
							}
						}
					}
				}
			}
			
			if (!shown)
				moo.reply(source, target, "Run fetch first")
		}
	}
}
