package net.rizon.moo.commands;

import java.util.Date;
import java.util.HashSet;

import net.rizon.moo.command;
import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;
import net.rizon.moo.split;

class message242 extends message
{
	public static HashSet<String> waiting_for = new HashSet<String>();
	public static String target_channel = null;
	public static String target_source = null;
	
	public message242()
	{
		super("242");
	}

	@Override
	public void run(String source, String[] message)
	{
		server s = server.findServerAbsolute(source);
		if (s == null)
			s = new server(source);
		
		String upstr = message[1];
		String[] tokens = upstr.split(" ");
		String[] times = tokens[4].split(":");
		
		int days, hours, mins, secs;
		try
		{
			days = Integer.parseInt(tokens[2]);
			hours = Integer.parseInt(times[0]);
			mins = Integer.parseInt(times[1]);
			secs = Integer.parseInt(times[2]);
		}
		catch (NumberFormatException ex)
		{
			ex.printStackTrace();
			return;
		}
		
		long total_ago = secs + (mins * 60) + (hours * 60 * 60) + (days * 60 * 60 * 24 );
		s.uptime = new Date(System.currentTimeMillis() - (total_ago * 1000L));
			
		waiting_for.remove(s.getName());
		
		if (waiting_for.isEmpty())
		{
			commandUptime.post_update(target_source, target_channel);
		}
	}
}

public class commandUptime extends command
{
	@SuppressWarnings("unused")
	private static message242 message_242 = new message242();
	
	public commandUptime(mpackage pkg)
	{
		super(pkg, "!UPTIME", "View server uptimes");
	}
	
	private static boolean only_extremes;
	private static String want_server;

	@Override
	public void execute(String source, String target, String[] params)
	{
		want_server = null;
		
		if (params.length > 1)
		{
			only_extremes = false;
			
			if (!params[1].equalsIgnoreCase("ALL"))
			{
				want_server = params[1];
			}
		}
		else
			only_extremes = true;
		
		boolean sent = false;
		for (server s : server.getServers())
		{
			if (s.isServices() == false && s.getSplit() == null && (want_server == null || moo.match(s.getName(), "*" + want_server + "*")))
			{
				moo.sock.write("STATS u " + s.getName());
				message242.waiting_for.add(s.getName());
				sent = true;
			}
		}
		if (sent == false)
		{
			moo.reply(source, target, "No servers match " + params[1]);
			return;
		}
		
		message242.target_channel = target;
		message242.target_source = source;
	}
	
	private static split findLastSplit(server s)
	{
		for (int i = s.getSplits().length; i > 0; --i)
		{
			split sp = s.getSplits()[i - 1];
			
			server serv = server.findServerAbsolute(sp.from);
			if (serv == null)
				continue;
			
			boolean b = false;
			for (int j = serv.getSplits().length; j > 0; --j)
			{
				split upsp = serv.getSplits()[j - 1];
				
				if (upsp.when.equals(sp.when))
					b = true;
			}
			if (b == true)
				continue;
			
			return sp;
		}
		
		return null;
	}
	
	private static int dashesFor(server s)
	{
		int longest = 0;
		for (server s2 : server.getServers())
		{
			int l = s2.getName().length();
			if (l > longest)
				longest = l;
		}
		
		return longest - s.getName().length() + 2;
	}
	
	private static String difference(Date now, Date then)
	{
		long lnow = now.getTime() / 1000L, lthen = then.getTime() / 1000L;
		
		long ldiff = now.compareTo(then) > 0 ? lnow - lthen : lthen - lnow;
		int days = 0, hours = 0, minutes = 0;
		
		if (ldiff == 0)
			return "0 seconds";
		
		while (ldiff > 86400)
		{
			++days;
			ldiff -= 86400;
		}
		while (ldiff > 3600)
		{
			++hours;
			ldiff -= 3600;
		}
		while (ldiff > 60)
		{
			++minutes;
			ldiff -= 60;
		}
		
		String buffer = "";
		if (days > 0)
			buffer += days + " day" + (days == 1 ? "" : "s") + " ";
		if (hours > 0)
			buffer += hours + " hour" + (hours == 1 ? "" : "s") + " ";
		if (minutes > 0)
			buffer += minutes + " minute" + (minutes == 1 ? "" : "s") + " ";
		if (ldiff > 0)
			buffer += ldiff + " second" + (ldiff == 1 ? "" : "s") + " ";
		buffer = buffer.trim();
		
		return buffer;
	}
	
	public static void post_update(String source, String target)
	{
		Date highest = null, lowest = null;
		split highest_sp = null, lowest_sp = null;
		Date now = new Date();
		
		for (server s : server.getServers())
		{
			if (s.isServices() || s.uptime == null)
				continue;
			else if (want_server != null && moo.match(s.getName(), "*" + want_server + "*") == false)
				continue;
			
			split sp = findLastSplit(s);
			
			if (highest == null || s.uptime.before(highest))
				highest = s.uptime;
			if (lowest == null || s.uptime.after(lowest))
				lowest = s.uptime;
			if (highest_sp == null || (sp != null && sp.when.before(highest_sp.when)))
				highest_sp = sp;
			if (lowest_sp == null || (sp != null && sp.when.after(lowest_sp.when)))
				lowest_sp = sp;
		}
		
		for (server s : server.getServers())
		{
			if (s.isServices() || s.uptime == null)
				continue;
			else if (want_server != null && moo.match(s.getName(), "*" + want_server + "*") == false)
				continue;

			boolean is_extreme = false;
			
			split sp = findLastSplit(s);
			int dashes = dashesFor(s);
			
			String buffer = "[UPTIME] " + s.getName() + " ";
			for (int i = 0; i < dashes; ++i)
				buffer += "-";
			buffer += " ";
			
			if (s.uptime == highest)
			{
				buffer += message.COLOR_GREEN;
				is_extreme = true;
			}
			else if (s.uptime == lowest)
			{
				buffer += message.COLOR_RED;
				is_extreme = true;
			}
			buffer += s.uptime.toString();
			buffer += message.COLOR_END;
			
			if (sp != null)
			{
				buffer += " - ";
				if (sp == highest_sp)
				{
					buffer += message.COLOR_GREEN;
					is_extreme = true;
				}
				else if (sp == lowest_sp)
				{
					buffer += message.COLOR_RED;
					is_extreme = true;
				}
				buffer += difference(now, sp.when);
				buffer += message.COLOR_END;
			}
			
			if (commandUptime.only_extremes)
			{
				if (is_extreme)
					moo.reply(source, target, buffer);
			}
			else
				moo.reply(source, target, buffer);
		}
	}
}
