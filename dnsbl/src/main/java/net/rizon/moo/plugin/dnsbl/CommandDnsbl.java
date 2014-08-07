package net.rizon.moo.plugin.dnsbl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;

class CommandDnsbl extends Command
{	
	private static HashSet<String> command_waiting_on = new HashSet<String>();
	private static String command_target_chan, command_target_source;
	private static boolean do_server_counts;
	private static String do_server_name;
	
	public CommandDnsbl(Plugin pkg)
	{
		super(pkg, "!DNSBL", "Views DNSBL counts");
		
		this.requiresChannel(Moo.conf.getList("staff_channels"));
		this.requiresChannel(Moo.conf.getList("oper_channels"));
		this.requiresChannel(Moo.conf.getList("admin_channels"));
	}
	
	@Override
	public void onHelp(String source)
	{
		Moo.notice(source, "Syntax: !DNSBL [SERVER [server.name]]");
		Moo.notice(source, "Fetches the amount of hits on all configured DNSBLs across the network.");
		Moo.notice(source, "If SERVER is given, the amount of DNSBL hits on the respective servers");
		Moo.notice(source, "will be shown. If a specific server name is appended to SERVER, the");
		Moo.notice(source, "number of hits on each DNSBL will be shown for that specific server.");
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		command_waiting_on.clear();
		command_target_chan = target;
		command_target_source = source;
		do_server_counts = false;
		do_server_name = null;
		
		if (params.length > 1 && params[1].equalsIgnoreCase("server"))
		{
			do_server_counts = true;
			
			if (params.length > 2)
				do_server_name = params[2];
		}

		for (Server s : Server.getServers())
			if (s.isNormal() && !s.isHub())
			{
				Moo.sock.write("STATS B " + s.getName());
				command_waiting_on.add(s.getName());
			}
	}
	
	static void checkReply(String source)
	{
		command_waiting_on.remove(source);
		if (command_waiting_on.isEmpty() && command_target_chan != null && command_target_source != null)
		{
			if (CommandDnsbl.do_server_name != null)
			{
				Server s = Server.findServer(do_server_name);
				if (s == null)
					Moo.reply(command_target_source, command_target_chan, "No servers found for " + do_server_name);
				else
				{
					DnsblInfo info = dnsbl.getDnsblInfoFor(s);
					long total = info.getTotal();

					Moo.reply(command_target_source, command_target_chan, "DNSBL counts for " + s.getName() + " (" + total + "):");

					String[] dnsbl_names = new String[info.hits.size()];
					info.hits.keySet().toArray(dnsbl_names);
					dnsblCountComparator.counts = info.hits;
					Arrays.sort(dnsbl_names, dnsblCountComparator.cmp);
					
					for (int i = dnsbl_names.length; i > 0; --i)
					{
						final String dnsbl_name = dnsbl_names[i - 1];
						long dnsbl_count = info.hits.get(dnsbl_name);
						
						float percent = total > 0 ? ((float) dnsbl_count / (float) total * 100) : 0;
						int percent_i = Math.round(percent);
						
						Moo.reply(CommandDnsbl.command_target_source, command_target_chan, dnsbl_name + ": " + dnsbl_count + " (" + percent_i + "%)");
					}
				}
			}
			else if (CommandDnsbl.do_server_counts)
			{
				long total = 0;
				for (Server s : Server.getServers())
					total += dnsbl.getDnsblInfoFor(s).getTotal();

				Moo.reply(CommandDnsbl.command_target_source, command_target_chan, "DNSBL counts by server (" + total + "):");

				Server servers[] = Server.getServers();
				Arrays.sort(servers, dnsblServerComparator.cmp);
				
				for (int i = servers.length; i > 0; --i)
				{
					Server s = servers[i - 1];
					long value = dnsbl.getDnsblInfoFor(s).getTotal();
					
					if (value == 0)
						continue;

					float percent = total > 0 ? ((float) value / (float) total * 100) : 0;
					int percent_i = Math.round(percent);
					
					Moo.reply(CommandDnsbl.command_target_source, command_target_chan, s.getName() + ": " + value + " (" + percent_i + "%)");
				}
			}
			else
			{
				HashMap<String, Long> dnsbl_counts = new HashMap<String, Long>();
				long total = 0;
				for (Server s : Server.getServers())
				{
					DnsblInfo info = dnsbl.getDnsblInfoFor(s);
					total += info.getTotal();
					
					for (Iterator<String> it = info.hits.keySet().iterator(); it.hasNext();)
					{
						final String dnsbl_name = it.next();
						long dnsbl_count = info.hits.get(dnsbl_name);
						
						long i = dnsbl_counts.containsKey(dnsbl_name) ? dnsbl_counts.get(dnsbl_name) : 0;
						i += dnsbl_count;
						dnsbl_counts.put(dnsbl_name, i);
					}
				}

				Moo.reply(CommandDnsbl.command_target_source, command_target_chan, "DNSBL counts (" + total + "):");

				String[] dnsbl_names = new String[dnsbl_counts.size()];
				dnsbl_counts.keySet().toArray(dnsbl_names);
				dnsblCountComparator.counts = dnsbl_counts;
				Arrays.sort(dnsbl_names, dnsblCountComparator.cmp);
				
				for (int i = dnsbl_names.length; i > 0; --i)
				{
					final String name = dnsbl_names[i - 1];
					long value = dnsbl_counts.get(name);
					
					float percent = total > 0 ? ((float) value / (float) total * 100) : 0;
					int percent_i = Math.round(percent);
					
					Moo.reply(CommandDnsbl.command_target_source, command_target_chan, name + ": " + value + " (" + percent_i + "%)");
				}
			}

			command_target_chan = command_target_source = null;
		}
	}
}
