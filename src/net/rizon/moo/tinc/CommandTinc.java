package net.rizon.moo.tinc;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.servercontrol.Connection;
import net.rizon.moo.servercontrol.Process;
import net.rizon.moo.servercontrol.ServerInfo;
import net.rizon.moo.servercontrol.servercontrol;
import net.rizon.moo.tinc.fetchers.ConfigurationFetcher;
import net.rizon.moo.tinc.fetchers.HostsFetcher;

class CommandTinc extends Command
{
	protected CommandTinc(Plugin pkg)
	{
		super(pkg, ".TINC", "Manages tinc configuration");
		this.requiresChannel(Moo.conf.getList("admin_channels"));
	}
	
	private void fetchConfiguration(Node n, String source, String target)
	{
		Connection c = Connection.findOrCreateConncetion(n.getServer().getServerInfo());
		Process p = ConfigurationFetcher.create(n, c, source, target);
		p.start();
	}
	
	private void fetchHosts(Node n, String source, String target)
	{
		Connection c = Connection.findOrCreateConncetion(n.getServer().getServerInfo());
		Process p = HostsFetcher.create(n, c, source, target);
		p.start();
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length > 1 && params[1].equalsIgnoreCase("HELP"))
		{
			Moo.reply(source, target, ".tinc - view overview");
			Moo.reply(source, target, ".tinc fetch server.name - update local copy of tinc configuration for the given server");
		}
		else if (params.length > 2 && params[1].equalsIgnoreCase("FETCH"))
		{
			String server_name = params[2];
			
			ServerInfo[] server_info = servercontrol.findServers(server_name, "ssh");
			if (server_info == null)
			{
				Moo.reply(source, target, "No servers found for " + server_name);
				return;
			}
			
			for (ServerInfo si : server_info)
			{
				Server serv = Server.findOrCreateServer(si);
									
				Moo.reply(source, target, "Fetching configuration for " + serv.getName() + "...");
				
				for (Layer l : tinc.Layers)
				{
					Node n = Node.getNode(serv, l);
					
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
			
			for (Layer l : tinc.Layers)
			{
				boolean shown_layer = false;

				Node[] nodes = Node.getNodesForLayer(l);
				for (Node n : nodes)
				{
					if (!n.name.isEmpty() && (search == null || Moo.matches(n.getServer().getName(), "*" + search + "*")))
					{
						String links = "";
						for (String link : n.connectTo)
							links += link + " ";
						links = links.trim();
						links = links.replaceAll(" ", ", ");
						
						if (!shown_layer)
						{
							Moo.reply(source, target, "Layer: " + l.getName());
							shown_layer = true;
						}
						
						Moo.reply(source, target, " Server: " + n.getServer().getName() + " Node: " + n.name + " Links: " + links);
						shown = true;
						
						if (n.getHost(n.name) == null)
							Moo.reply(source, target, "  " + n.name + " has no self configuration");
						
						String invalidConnectTos = "";
						for (String link : n.connectTo)
						{
							boolean found = false;
							
							for (Host host : n.hosts)
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
							Moo.reply(source, target, "  " + n.name + " has ConnectTo but no host file for: " + invalidConnectTos);
						
						String missingConnectTos = "";
						String unknownHosts = "";
						String outOfDate = "";
						for (Host host : n.hosts)
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
							
							Node target_node = Node.getNodeWithConfigName(n.getLayer(), host.name);
							if (target_node == null)
							{
								unknownHosts += " " + host.name;
							}
							else
							{
								Host target_host = target_node.getHost(host.name);
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
							Moo.reply(source, target, "  " + n.name + " has host file but no ConnectTo for: " + missingConnectTos);
							
						unknownHosts = unknownHosts.trim().replaceAll(" ", ", ");
						if (!unknownHosts.isEmpty())
							Moo.reply(source, target, "  " + n.name + " has host file for unknown host(s): " + unknownHosts);
						
						outOfDate = outOfDate.trim().replaceAll(" ", ", ");
						if (!outOfDate.isEmpty())
							Moo.reply(source, target, "  " + n.name + " has out of date host file for: " + outOfDate);
					}
				}
			}
			
			if (!shown)
				Moo.reply(source, target, "Run fetch first");
		}
	}
}
