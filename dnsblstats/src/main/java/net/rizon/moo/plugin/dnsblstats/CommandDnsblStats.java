package net.rizon.moo.plugin.dnsblstats;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;
import net.rizon.moo.plugin.dnsblstats.comparators.CountComparator;
import net.rizon.moo.plugin.dnsblstats.comparators.ServerComparator;

class CommandDnsblStats extends Command
{
	private static Set<String> command_waiting_on = new HashSet<>();
	private static CommandSource command_source;
	private static boolean do_server_counts;
	private static String do_server_name;

	@Inject
	private ServerManager serverManager;

	@Inject
	private Protocol protocol;

	@Inject
	private dnsblstats dnsblstats;

	@Inject
	private CountComparator countComparator;

	@Inject
	private ServerComparator serverComparator;

	@Inject
	public CommandDnsblStats(Config conf)
	{
		super("!DNSBLSTATS", "Views DNSBL counts");

		this.requiresChannel(conf.staff_channels);
		this.requiresChannel(conf.oper_channels);
		this.requiresChannel(conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !DNSBLSTATS [SERVER [server.name]]");
		source.notice("Fetches the amount of hits on all configured DNSBLs across the network.");
		source.notice("If SERVER is given, the amount of DNSBL hits on the respective servers");
		source.notice("will be shown. If a specific server name is appended to SERVER, the");
		source.notice("number of hits on each DNSBL will be shown for that specific server.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		command_waiting_on.clear();
		command_source = source;
		do_server_counts = false;
		do_server_name = null;

		if (params.length > 1 && params[1].equalsIgnoreCase("server"))
		{
			do_server_counts = true;

			if (params.length > 2)
				do_server_name = params[2];
		}

		for (Server s : serverManager.getServers())
			if (s.isNormal() && !s.isHub())
			{
				protocol.write("STATS", "B", s.getName());
				command_waiting_on.add(s.getName());
			}
	}

	void checkReply(String source)
	{
		command_waiting_on.remove(source);
		if (command_waiting_on.isEmpty() && command_source != null)
		{
			if (CommandDnsblStats.do_server_name != null)
			{
				Server s = serverManager.findServer(do_server_name);
				if (s == null)
					command_source.reply("No servers found for " + do_server_name);
				else
				{
					DnsblInfo info = dnsblstats.getDnsblInfoFor(s);
					long total = info.getTotal();

					command_source.reply("DNSBL counts for " + s.getName() + " (" + total + "):");

					String[] dnsbl_names = new String[info.hits.size()];
					info.hits.keySet().toArray(dnsbl_names);
					countComparator.counts = info.hits;
					Arrays.sort(dnsbl_names, countComparator);

					for (int i = dnsbl_names.length; i > 0; --i)
					{
						final String dnsbl_name = dnsbl_names[i - 1];
						long dnsbl_count = info.hits.get(dnsbl_name);

						float percent = total > 0 ? ((float) dnsbl_count / (float) total * 100) : 0;
						int percent_i = Math.round(percent);

						command_source.reply(dnsbl_name + ": " + dnsbl_count + " (" + percent_i + "%)");
					}
				}
			}
			else if (CommandDnsblStats.do_server_counts)
			{
				long total = 0;
				for (Server s : serverManager.getServers())
					total += dnsblstats.getDnsblInfoFor(s).getTotal();

				command_source.reply("DNSBL counts by server (" + total + "):");

				Server servers[] = serverManager.getServers();
				Arrays.sort(servers, serverComparator);

				for (int i = servers.length; i > 0; --i)
				{
					Server s = servers[i - 1];
					long value = dnsblstats.getDnsblInfoFor(s).getTotal();

					if (value == 0)
						continue;

					float percent = total > 0 ? ((float) value / (float) total * 100) : 0;
					int percent_i = Math.round(percent);

					command_source.reply(s.getName() + ": " + value + " (" + percent_i + "%)");
				}
			}
			else
			{
				HashMap<String, Long> dnsbl_counts = new HashMap<String, Long>();
				long total = 0;
				for (Server s : serverManager.getServers())
				{
					DnsblInfo info = dnsblstats.getDnsblInfoFor(s);
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

				command_source.reply("DNSBL counts (" + total + "):");

				String[] dnsbl_names = new String[dnsbl_counts.size()];
				dnsbl_counts.keySet().toArray(dnsbl_names);
				countComparator.counts = dnsbl_counts;
				Arrays.sort(dnsbl_names, countComparator);

				for (int i = dnsbl_names.length; i > 0; --i)
				{
					final String name = dnsbl_names[i - 1];
					long value = dnsbl_counts.get(name);

					float percent = total > 0 ? ((float) value / (float) total * 100) : 0;
					int percent_i = Math.round(percent);

					command_source.reply(name + ": " + value + " (" + percent_i + "%)");
				}
			}

			command_source = null;
		}
	}
}
