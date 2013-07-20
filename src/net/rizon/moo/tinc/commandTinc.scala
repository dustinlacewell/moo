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
			var search: String = null
			if (params.size > 1)
				search = params(1)
			
			var shown = false
			
			for (layer <- tinc.layers)
			{
				var shown_layer = false
				
				var nodes: LinkedList[node] = node.getNodesForLayer(layer)
				for (n <- nodes)
				{
					if (!n.name.isEmpty() && (search == null || moo.matches(n.getServer().getName(), "*" + search + "*")))
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
						
						if (n.getHost(n.name) == null)
							moo.reply(source, target, "  " + n.name + " has no self configuration")
						
						var invalidConnectTos = ""
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
								invalidConnectTos += " " + link
							}
						}
						invalidConnectTos = invalidConnectTos.trim.replaceAll(" ", ", ")
						if (!invalidConnectTos.isEmpty)
							moo.reply(source, target, "  " + n.name + " has ConnectTo but no host file for: " + invalidConnectTos)
						
						var missingConnectTos = "";
						var unknownHosts = ""
						var outOfDate = ""
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
								missingConnectTos += " " + host.name
							}
							
							var target_node = node.getNodeWithConfigName(n.getLayer(), host.name)
							if (target_node == null)
							{
								unknownHosts += " " + host.name
							}
							
							var target_host = target_node.getHost(host.name)
							if (target_host != null)
							{
								// if target_host is null then target has no host configuration for itself, and this
								// will be warned about later
								
								if (!target_host.equals(host))
								{
									outOfDate += " " + host.name
								}
							}
						}
						
						missingConnectTos = missingConnectTos.trim.replaceAll(" ", ", ")
						if (!missingConnectTos.isEmpty)
							moo.reply(source, target, "  " + n.name + " has host file but no ConnectTo for: " + missingConnectTos)
							
						unknownHosts = unknownHosts.trim.replaceAll(" ", ", ")
						if (!unknownHosts.isEmpty)
							moo.reply(source, target, "  " + n.name + " has host file for unknown host(s): " + unknownHosts)
						
						outOfDate = outOfDate.trim.replaceAll(" ", ", ")
						if (!outOfDate.isEmpty)
							moo.reply(source, target, "  " + n.name + " has out of date host file for: " + outOfDate)
					}
				}
			}
			
			if (!shown)
				moo.reply(source, target, "Run fetch first")
		}
	}
}
