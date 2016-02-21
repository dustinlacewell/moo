package net.rizon.moo.plugin.dnsblstats;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;


class StatsRequester implements Runnable
{
	private static boolean check_requested;
	private static boolean first_run = true;
	private static Set<String> requested = new HashSet<>(), check_waiting_on = new HashSet<>();
	private static long before_total_count;
	private static Map<String, Long> before_count = new HashMap<>();

	@Inject
	private ServerManager serverManager;

	@Inject
	private Protocol protocol;

	@Inject
	private dnsblstats dnsblstats;

	@Inject
	private Config config;

	@Override
	public void run()
	{
		check_requested = true;
		requested.clear();
		check_waiting_on.clear();
		before_total_count = 0;
		before_count.clear();

		for (Server s : serverManager.getServers())
			if (s.isNormal() && !s.isHub())
			{
				protocol.write("STATS", "B", s.getName());

				requested.add(s.getName());
				check_waiting_on.add(s.getName());

				long count = dnsblstats.getDnsblInfoFor(s).getTotal();
				before_total_count += count;
				before_count.put(s.getName(), count);
			}
	}

	private static final long global_threshold = 50;
	private static final long server_threshold = 20;

	void checkWarn(String source)
	{
		check_waiting_on.remove(source);

		if (!check_requested || !check_waiting_on.isEmpty())
			return;

		long after_total_count = 0;
		HashMap<String, Long> after_counts = new HashMap<String, Long>();

		for (String ss : requested)
		{
			Server s = serverManager.findServerAbsolute(ss);
			if (s != null && s.getSplit() == null && !s.isServices())
			{
				long count = dnsblstats.getDnsblInfoFor(s).getTotal();
				after_total_count += count;
				after_counts.put(s.getName(), count);
			}
		}

		String dnsbl_message = "";

		long global_change = after_total_count - before_total_count;
		if (global_change >= global_threshold)
			dnsbl_message = "DNSBL WARN: " + global_change + " in 60s";

		for (Iterator<String> it = before_count.keySet().iterator(); it.hasNext();)
		{
			Server s = serverManager.findServerAbsolute(it.next());
			if (s == null)
				continue;

			long before_count = StatsRequester.before_count.get(s.getName());
			long after_count = after_counts.get(s.getName());

			long server_change = after_count - before_count;
			if (server_change >= server_threshold)
			{
				if (!dnsbl_message.isEmpty())
					dnsbl_message += "; " + s.getName() + ": " + server_change + " in 60s";
				else
					dnsbl_message = "DNSBL WARN " + s.getName() + ": " + server_change + " in 60s";
			}
		}

		if (!dnsbl_message.isEmpty() && !first_run)
			protocol.privmsgAll(config.oper_channels, dnsbl_message);

		first_run = false;
		check_requested = false;
		requested.clear();
		before_total_count = 0;
		before_count.clear();
	}
}
