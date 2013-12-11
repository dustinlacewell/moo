package net.rizon.moo.commands;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import net.rizon.moo.Command;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.MPackage;
import net.rizon.moo.Server;
import net.rizon.moo.Timer;

class dnsblServerComparator implements Comparator<Server>
{
	public static dnsblServerComparator cmp = new dnsblServerComparator();

	@Override
	public int compare(Server arg0, Server arg1)
	{
		long val0 = CommandDnsbl.getDnsblFor(arg0), val1 = CommandDnsbl.getDnsblFor(arg1);
		if (val0 < val1)
			return -1;
		else if (val0 > val1)
			return 1;
		else
			return 0;
	}
}

class dnsblCountComparator implements Comparator<String>
{
	public static dnsblCountComparator cmp = new dnsblCountComparator();
	public static HashMap<String, Long> counts = null;
	
	@Override
	public int compare(String arg0, String arg1)
	{
		long val0 = counts.get(arg0), val1 = counts.get(arg1);
		if (val0 < val1)
			return -1;
		else if (val0 > val1)
			return 1;
		else
			return 0;
	}
}

class message219_dnsbl extends Message
{
	public message219_dnsbl()
	{
		super("219");
	}
	
	private static final long global_threshold = 50;
	private static final long server_threshold = 20;
	
	@Override
	public void run(String source, String[] message)
	{
		if (message[1].equals("B") == false)
			return;
		
		CommandDnsbl.command_waiting_on.remove(source);
		dnsblTimer.check_waiting_on.remove(source);
		
		if (CommandDnsbl.command_waiting_on.isEmpty() && CommandDnsbl.command_target_chan != null && CommandDnsbl.command_target_source != null)
		{
			if (CommandDnsbl.do_server_name != null)
			{
				Server s = Server.findServer(CommandDnsbl.do_server_name);
				if (s == null)
					Moo.reply(CommandDnsbl.command_target_source, CommandDnsbl.command_target_chan, "No servers found for " + CommandDnsbl.do_server_name);
				else
				{
					long total = CommandDnsbl.getDnsblFor(s);

					Moo.reply(CommandDnsbl.command_target_source, CommandDnsbl.command_target_chan, "DNSBL counts for " + s.getName() + " (" + total + "):");

					String[] dnsbl_names = new String[s.dnsbl.size()];
					s.dnsbl.keySet().toArray(dnsbl_names);
					dnsblCountComparator.counts = s.dnsbl;
					Arrays.sort(dnsbl_names, dnsblCountComparator.cmp);
					
					for (int i = dnsbl_names.length; i > 0; --i)
					{
						final String dnsbl_name = dnsbl_names[i - 1];
						long dnsbl_count = s.dnsbl.get(dnsbl_name);
						
						float percent = total > 0 ? ((float) dnsbl_count / (float) total * (float) 100) : 0;
						int percent_i = Math.round(percent);
						
						Moo.reply(CommandDnsbl.command_target_source, CommandDnsbl.command_target_chan, dnsbl_name + ": " + dnsbl_count + " (" + percent_i + "%)");
					}
				}
			}
			else if (CommandDnsbl.do_server_counts)
			{
				long total = 0;
				for (Server s : Server.getServers())
					total += CommandDnsbl.getDnsblFor(s);

				Moo.reply(CommandDnsbl.command_target_source, CommandDnsbl.command_target_chan, "DNSBL counts by server (" + total + "):");

				Server servers[] = Server.getServers();
				Arrays.sort(servers, dnsblServerComparator.cmp);
				
				for (int i = servers.length; i > 0; --i)
				{
					Server s = servers[i - 1];
					long value = CommandDnsbl.getDnsblFor(s);
					
					if (value == 0)
						continue;

					float percent = total > 0 ? ((float) value / (float) total * (float) 100) : 0;
					int percent_i = Math.round(percent);
					
					Moo.reply(CommandDnsbl.command_target_source, CommandDnsbl.command_target_chan, s.getName() + ": " + value + " (" + percent_i + "%)");
				}
			}
			else
			{
				HashMap<String, Long> dnsbl_counts = new HashMap<String, Long>();
				long total = 0;
				for (Server s : Server.getServers())
				{
					total += CommandDnsbl.getDnsblFor(s);
					
					for (Iterator<String> it = s.dnsbl.keySet().iterator(); it.hasNext();)
					{
						final String dnsbl_name = it.next();
						long dnsbl_count = s.dnsbl.get(dnsbl_name);
						
						long i = dnsbl_counts.containsKey(dnsbl_name) ? dnsbl_counts.get(dnsbl_name) : 0;
						i += dnsbl_count;
						dnsbl_counts.put(dnsbl_name, i);
					}
				}

				Moo.reply(CommandDnsbl.command_target_source, CommandDnsbl.command_target_chan, "DNSBL counts (" + total + "):");

				String[] dnsbl_names = new String[dnsbl_counts.size()];
				dnsbl_counts.keySet().toArray(dnsbl_names);
				dnsblCountComparator.counts = dnsbl_counts;
				Arrays.sort(dnsbl_names, dnsblCountComparator.cmp);
				
				for (int i = dnsbl_names.length; i > 0; --i)
				{
					final String name = dnsbl_names[i - 1];
					long value = dnsbl_counts.get(name);
					
					float percent = total > 0 ? ((float) value / (float) total * (float) 100) : 0;
					int percent_i = Math.round(percent);
					
					Moo.reply(CommandDnsbl.command_target_source, CommandDnsbl.command_target_chan, name + ": " + value + " (" + percent_i + "%)");
				}
			}
				
			CommandDnsbl.command_target_chan = CommandDnsbl.command_target_source = null;
		}
		
		if (dnsblTimer.check_waiting_on.isEmpty() && dnsblTimer.check_requested && Moo.conf.getOperChannels().length > 0)
		{
			long after_total_count = 0;
			HashMap<String, Long> after_counts = new HashMap<String, Long>();
			
			for (String ss : dnsblTimer.requested)
			{
				Server s = Server.findServerAbsolute(ss);
				if (s != null && s.getSplit() == null && !s.isServices())
				{
					long count = CommandDnsbl.getDnsblFor(s);
					after_total_count += count;
					after_counts.put(s.getName(), count);
				}
			}
			
			String dnsbl_message = "";
			
			long global_change = after_total_count - dnsblTimer.before_total_count;
			if (global_change >= global_threshold)
				dnsbl_message = "DNSBL WARN: " + global_change + " in 60s";
			
			for (Iterator<String> it = dnsblTimer.before_count.keySet().iterator(); it.hasNext();)
			{
				Server s = Server.findServerAbsolute(it.next());
				if (s == null)
					continue;
				
				long before_count = dnsblTimer.before_count.get(s.getName());
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
			
			if (!dnsbl_message.isEmpty())
				for (final String chan : Moo.conf.getOperChannels())
					Moo.privmsg(chan, dnsbl_message);
			
			dnsblTimer.check_requested = false;
			dnsblTimer.requested.clear();
			dnsblTimer.before_total_count = 0;
			dnsblTimer.before_count.clear();
		}
	}
}

class dnsblTimer extends Timer
{
	public static boolean check_requested;
	public static HashSet<String> requested = new HashSet<String>(), check_waiting_on = new HashSet<String>();
	public static long before_total_count;
	public static HashMap<String, Long> before_count = new HashMap<String, Long>();
	
	public dnsblTimer()
	{
		super(60, true);
	}

	@Override
	public void run(Date now)
	{
		check_requested = true;
		requested.clear();
		check_waiting_on.clear();
		before_total_count = 0;
		before_count.clear();

		for (Server s : Server.getServers())
			if (s.getSplit() == null && !s.isServices())
			{
				Moo.sock.write("STATS B " + s.getName());
				requested.add(s.getName());
				check_waiting_on.add(s.getName());
				
				long count = CommandDnsbl.getDnsblFor(s);
				before_total_count += count;
				before_count.put(s.getName(), count);
			}
	}
}

class CommandDnsbl extends Command
{
	@SuppressWarnings("unused")
	private static message219_dnsbl msg_219 = new message219_dnsbl();
	
	public static HashSet<String> command_waiting_on = new HashSet<String>();
	public static String command_target_chan, command_target_source;
	public static boolean do_server_counts;
	public static String do_server_name;
	
	private dnsblTimer dnsbl_timer;
	
	public CommandDnsbl(MPackage pkg)
	{
		super(pkg, "!DNSBL", "Views DNSBL counts");
		
		this.dnsbl_timer = new dnsblTimer();
		this.dnsbl_timer.start();
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
			if (s.getSplit() == null && !s.isServices())
			{
				Moo.sock.write("STATS B " + s.getName());
				command_waiting_on.add(s.getName());
			}
	}
	
	public static long getDnsblFor(Server s)
	{
		long i = 0;
		for (Iterator<String> it = s.dnsbl.keySet().iterator(); it.hasNext();)
			i += s.dnsbl.get(it.next());
		return i;
	}
}
