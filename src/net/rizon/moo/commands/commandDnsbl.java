package net.rizon.moo.commands;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import net.rizon.moo.command;
import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;
import net.rizon.moo.timer;

class dnsblServerComparator implements Comparator<server>
{
	public static dnsblServerComparator cmp = new dnsblServerComparator();

	@Override
	public int compare(server arg0, server arg1)
	{
		long val0 = commandDnsbl.getDnsblFor(arg0), val1 = commandDnsbl.getDnsblFor(arg1);
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

class message219_dnsbl extends message
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
		
		commandDnsbl.command_waiting_on.remove(source);
		dnsblTimer.check_waiting_on.remove(source);
		
		if (commandDnsbl.command_waiting_on.isEmpty() && commandDnsbl.command_target_chan != null && commandDnsbl.command_target_source != null)
		{
			if (commandDnsbl.do_server_name != null)
			{
				server s = server.findServer(commandDnsbl.do_server_name);
				if (s == null)
					moo.reply(commandDnsbl.command_target_source, commandDnsbl.command_target_chan, "No servers found for " + commandDnsbl.do_server_name);
				else
				{
					moo.reply(commandDnsbl.command_target_source, commandDnsbl.command_target_chan, "DNSBL counts for " + s.getName() + ":");
					
					long total = commandDnsbl.getDnsblFor(s);
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
						
						moo.reply(commandDnsbl.command_target_source, commandDnsbl.command_target_chan, dnsbl_name + ": " + dnsbl_count + " (" + percent_i + "%)");
					}
				}
			}
			else if (commandDnsbl.do_server_counts)
			{
				moo.reply(commandDnsbl.command_target_source, commandDnsbl.command_target_chan, "DNSBL counts by server:");

				long total = 0;
				for (server s : server.getServers())
					total += commandDnsbl.getDnsblFor(s);
				
				server servers[] = server.getServers();
				Arrays.sort(servers, dnsblServerComparator.cmp);
				
				for (int i = servers.length; i > 0; --i)
				{
					server s = servers[i - 1];
					long value = commandDnsbl.getDnsblFor(s);
					
					if (value == 0)
						continue;

					float percent = total > 0 ? ((float) value / (float) total * (float) 100) : 0;
					int percent_i = Math.round(percent);
					
					moo.reply(commandDnsbl.command_target_source, commandDnsbl.command_target_chan, s.getName() + ": " + value + " (" + percent_i + "%)");
				}
			}
			else
			{
				moo.reply(commandDnsbl.command_target_source, commandDnsbl.command_target_chan, "DNSBL counts:");

				HashMap<String, Long> dnsbl_counts = new HashMap<String, Long>();
				long total = 0;
				for (server s : server.getServers())
				{
					total += commandDnsbl.getDnsblFor(s);
					
					for (Iterator<String> it = s.dnsbl.keySet().iterator(); it.hasNext();)
					{
						final String dnsbl_name = it.next();
						long dnsbl_count = s.dnsbl.get(dnsbl_name);
						
						long i = dnsbl_counts.containsKey(dnsbl_name) ? dnsbl_counts.get(dnsbl_name) : 0;
						i += dnsbl_count;
						dnsbl_counts.put(dnsbl_name, i);
					}
				}
				
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
					
					moo.reply(commandDnsbl.command_target_source, commandDnsbl.command_target_chan, name + ": " + value + " (" + percent_i + "%)");
				}
			}
				
			commandDnsbl.command_target_chan = commandDnsbl.command_target_source = null;
		}
		
		if (dnsblTimer.check_waiting_on.isEmpty() && dnsblTimer.check_requested && moo.conf.getDnsblChannels().length > 0)
		{
			long after_total_count = 0;
			HashMap<String, Long> after_counts = new HashMap<String, Long>();
			
			for (String ss : dnsblTimer.requested)
			{
				server s = server.findServerAbsolute(ss);
				if (s != null && s.getSplit() == null && !s.isServices())
				{
					long count = commandDnsbl.getDnsblFor(s);
					after_total_count += count;
					after_counts.put(s.getName(), count);
				}
			}
			
			long global_change = after_total_count - dnsblTimer.before_total_count;
			if (global_change >= global_threshold)
				for (final String chan : moo.conf.getDnsblChannels())
					moo.privmsg(chan, "DNSBL WARN: " + global_change + " in 60s");
			
			for (Iterator<String> it = dnsblTimer.before_count.keySet().iterator(); it.hasNext();)
			{
				server s = server.findServerAbsolute(it.next());
				if (s == null)
					continue;
				
				long before_count = dnsblTimer.before_count.get(s.getName());
				long after_count = after_counts.get(s.getName());
				
				long server_change = after_count - before_count;
				if (server_change >= server_threshold)
					for (final String chan : moo.conf.getDnsblChannels())
						moo.privmsg(chan, "DNSBL WARN " + s.getName() + ": " + server_change + " in 60s");
			}
			
			dnsblTimer.check_requested = false;
			dnsblTimer.requested.clear();
			dnsblTimer.before_total_count = 0;
			dnsblTimer.before_count.clear();
		}
	}
}

class dnsblTimer extends timer
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

		for (server s : server.getServers())
			if (s.getSplit() == null && !s.isServices())
			{
				moo.sock.write("STATS B " + s.getName());
				requested.add(s.getName());
				check_waiting_on.add(s.getName());
				
				long count = commandDnsbl.getDnsblFor(s);
				before_total_count += count;
				before_count.put(s.getName(), count);
			}
	}
}

public class commandDnsbl extends command
{
	@SuppressWarnings("unused")
	private static message219_dnsbl msg_219 = new message219_dnsbl();
	
	public static HashSet<String> command_waiting_on = new HashSet<String>();
	public static String command_target_chan, command_target_source;
	public static boolean do_server_counts;
	public static String do_server_name;
	
	private dnsblTimer dnsbl_timer;
	
	public commandDnsbl(mpackage pkg)
	{
		super(pkg, "!DNSBL", "Views DNSBL counts");
		
		this.dnsbl_timer = new dnsblTimer();
		this.dnsbl_timer.start();
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

		for (server s : server.getServers())
			if (s.getSplit() == null && !s.isServices())
			{
				moo.sock.write("STATS B " + s.getName());
				command_waiting_on.add(s.getName());
			}
	}
	
	public static long getDnsblFor(server s)
	{
		long i = 0;
		for (Iterator<String> it = s.dnsbl.keySet().iterator(); it.hasNext();)
			i += s.dnsbl.get(it.next());
		return i;
	}
}
