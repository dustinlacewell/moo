package net.rizon.moo.tinc;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.servercontrol.connection;
import net.rizon.moo.servercontrol.process;
import net.rizon.moo.servercontrol.serverInfo;
import net.rizon.moo.servercontrol.servercontrol;
import net.rizon.moo.tinc.fetchers.configurationFetcher;
import net.rizon.moo.tinc.fetchers.hostsFetcher;

class commandTinc extends command
{
	protected commandTinc(mpackage pkg)
	{
		super(pkg, ".TINC", "Manages tinc configuration");
		this.requiresChannel(moo.conf.getAdminChannels());
	}
	
	private void fetchConfiguration(node n, String source, String target)
	{
		connection c = connection.findOrCreateConncetion(n.getServer().getServerInfo());
		process p = configurationFetcher.create(n, c, source, target);
		p.start();
	}
	
	private void fetchHosts(node n, String source, String target)
	{
		connection c = connection.findOrCreateConncetion(n.getServer().getServerInfo());
		process p = hostsFetcher.create(n, c, source, target);
		p.start();
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length > 1 && params[1].equalsIgnoreCase("HELP"))
		{
			moo.reply(source, target, ".tinc - view overview");
			moo.reply(source, target, ".tinc fetch server.name - update local copy of tinc configuration for the given server");
		}
		else if (params.length > 2 && params[1].equalsIgnoreCase("FETCH"))
		{
			String server_name = params[2];
			
			serverInfo[] server_info = servercontrol.findServers(server_name, "ssh");
			if (server_info == null)
			{
				moo.reply(source, target, "No servers found for " + server_name);
				return;
			}
			
			for (serverInfo si : server_info)
			{
				server serv = server.findOrCreateServer(si);
									
				moo.reply(source, target, "Fetching configuration for " + serv.getName() + "...");
				
				for (layer l : tinc.Layers)
				{
					node n = node.getNode(serv, l);
					
					fetchConfiguration(n, source, target);
					fetchHosts(n, source, target);
				}
			}
		}
		else
		{
			String search = null;
			if (params.length > 1)
				search = params[1];
			
			boolean shown = false;
			
			for (layer l : tinc.Layers)
			{
				boolean shown_layer = false;

				node[] nodes = node.getNodesForLayer(l);
				for (node n : nodes)
				{
					if (!n.name.isEmpty() && (search == null || moo.matches(n.getServer().getName(), "*" + search + "*")))
					{
						String links = "";
						for (String link : n.connectTo)
							links += link + " ";
						links = links.trim();
						links = links.replaceAll(" ", ", ");
						
						if (!shown_layer)
						{
							moo.reply(source, target, "Layer: " + l.getName());
							shown_layer = true;
						}
						
						moo.reply(source, target, " Server: " + n.getServer().getName() + " Node: " + n.name + " Links: " + links);
						shown = true;
						
						if (n.getHost(n.name) == null)
							moo.reply(source, target, "  " + n.name + " has no self configuration");
						
						String invalidConnectTos = "";
						for (String link : n.connectTo)
						{
							boolean found = false;
							
							for (host host : n.hosts)
							{
								if (host.name.equals(link))
								{
									found = true;
								}
							}
							
							if (!found)
							{
								invalidConnectTos += " " + link;
							}
						}
						invalidConnectTos = invalidConnectTos.trim().replaceAll(" ", ", ");
						if (!invalidConnectTos.isEmpty())
							moo.reply(source, target, "  " + n.name + " has ConnectTo but no host file for: " + invalidConnectTos);
						
						String missingConnectTos = "";
						String unknownHosts = "";
						String outOfDate = "";
						for (host host : n.hosts)
						{
							boolean found = false;
							
							for (String link : n.connectTo)
							{
								if (host.name.equals(link))
								{
									found = true;
								}
							}
							
							if (!found && !n.name.equals(host.name))
							{
								// Nodes are supposed to have hosts for themselves with no ConnectTo
								missingConnectTos += " " + host.name;
							}
							
							node target_node = node.getNodeWithConfigName(n.getLayer(), host.name);
							if (target_node == null)
							{
								unknownHosts += " " + host.name;
							}
							else
							{
								host target_host = target_node.getHost(host.name);
								if (target_host != null)
								{
									// if target_host is null then target has no host configuration for itself, and this
									// will be warned about later
									
									if (!target_host.equals(host))
									{
										outOfDate += " " + host.name;
									}
								}
							}
						}
						
						missingConnectTos = missingConnectTos.trim().replaceAll(" ", ", ");
						if (!missingConnectTos.isEmpty())
							moo.reply(source, target, "  " + n.name + " has host file but no ConnectTo for: " + missingConnectTos);
							
						unknownHosts = unknownHosts.trim().replaceAll(" ", ", ");
						if (!unknownHosts.isEmpty())
							moo.reply(source, target, "  " + n.name + " has host file for unknown host(s): " + unknownHosts);
						
						outOfDate = outOfDate.trim().replaceAll(" ", ", ");
						if (!outOfDate.isEmpty())
							moo.reply(source, target, "  " + n.name + " has out of date host file for: " + outOfDate);
					}
				}
			}
			
			if (!shown)
				moo.reply(source, target, "Run fetch first");
		}
	}
}
