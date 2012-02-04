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

class dnsblComparator implements Comparator<String>
{
	public static dnsblComparator cmp = new dnsblComparator();

	@Override
	public int compare(String arg0, String arg1)
	{
		long val0 = commandDnsbl.command_dnsbl_values.get(arg0), val1 = commandDnsbl.command_dnsbl_values.get(arg1);
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
			moo.sock.reply(commandDnsbl.command_target_source, commandDnsbl.command_target_chan, "DNSBL counts:");
			
			long total = 0;
			for (Iterator<String> it = commandDnsbl.command_dnsbl_values.keySet().iterator(); it.hasNext();)
				total += commandDnsbl.command_dnsbl_values.get(it.next());
			
			String keys_sorted[] = new String[commandDnsbl.command_dnsbl_values.size()];
			commandDnsbl.command_dnsbl_values.keySet().toArray(keys_sorted);
			Arrays.sort(keys_sorted, dnsblComparator.cmp);
			
			for (int i = keys_sorted.length; i > 0; --i)
			{
				String name = keys_sorted[i - 1];
				long value = commandDnsbl.command_dnsbl_values.get(name);
				float percent = total > 0 ? ((float) value / (float) total * (float) 100) : 0;
				int percent_i = Math.round(percent);
				
				moo.sock.reply(commandDnsbl.command_target_source, commandDnsbl.command_target_chan, name + ": " + value + " (" + percent_i + "%)");
			}
			
			commandDnsbl.command_dnsbl_values.clear();
			commandDnsbl.command_target_chan = commandDnsbl.command_target_source = null;
		}
		
		if (dnsblTimer.check_waiting_on.isEmpty() && dnsblTimer.check_requested && moo.conf.getDnsblChannels().length > 0)
		{
			long after_total_count = 0;
			HashMap<String, Long> after_counts = new HashMap<String, Long>();
			for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
			{
				server s = it.next();
				
				long count = commandDnsbl.getDnsblFor(s);
				after_total_count += count;
				after_counts.put(s.getName(), count);
			}
			
			long global_change = after_total_count - dnsblTimer.before_total_count;
			if (global_change >= global_threshold)
				for (final String chan : moo.conf.getDnsblChannels())
					moo.sock.privmsg(chan, "DNSBL WARN: " + global_change + " in 60s");
			
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
						moo.sock.privmsg(chan, "DNSBL WARN " + s.getName() + ": " + server_change + " in 60s");
			}
			
			dnsblTimer.check_requested = false;
		}
	}
}

class dnsblTimer extends timer
{
	public static boolean check_requested;
	public static HashSet<String> check_waiting_on = new HashSet<String>();
	public static long before_total_count;
	public static HashMap<String, Long> before_count = new HashMap<String, Long>();
	
	public dnsblTimer()
	{
		super(60, true);
	}

	@Override
	public void run(Date now)
	{
		check_waiting_on.clear();
		before_total_count = 0;
		before_count.clear();
		check_requested = true;

		for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
		{
			server s = it.next();
			if (s.getSplit() == null && !s.isServices())
			{
				moo.sock.write("STATS B " + s.getName());
				check_waiting_on.add(s.getName());
				
				long count = commandDnsbl.getDnsblFor(s);
				before_total_count += count;
				before_count.put(s.getName(), count);
			}
		}
	}
}

public class commandDnsbl extends command
{
	@SuppressWarnings("unused")
	private static message219_dnsbl msg_219 = new message219_dnsbl();
	
	public static HashSet<String> command_waiting_on = new HashSet<String>();
	public static HashMap<String, Long> command_dnsbl_values = new HashMap<String, Long>();
	public static String command_target_chan, command_target_source;
	
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
		command_dnsbl_values.clear();
		command_target_chan = target;
		command_target_source = source;

		for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
		{
			server s = it.next();
			if (s.getSplit() == null && !s.isServices())
			{
				moo.sock.write("STATS B " + s.getName());
				command_waiting_on.add(s.getName());
			}
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
