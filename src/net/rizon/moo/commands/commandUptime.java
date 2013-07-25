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

class commandUptime extends command
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
	public void onHelp(String source)
	{
		moo.notice(source, "Syntax: !UPTIME [{ ALL | server.name }]");
		moo.notice(source, "Without any parameters, the highest and lowest uptime and last split times");
		moo.notice(source, "are sought and shown.");
		moo.notice(source, "If ALL is given, uptimes and times since the last split for all servers will");
		moo.notice(source, "be shown.");
		moo.notice(source, "If a server name is given, the uptime and last split time for that particular");
		moo.notice(source, "will be shown.");
		moo.notice(source, "The lowest last split time and the highest uptime will be colored "
		                   + message.COLOR_GREEN + "green" + message.COLOR_END + ",");
		moo.notice(source, "the highest last split time will be colored "
		                   + message.COLOR_RED + "red" + message.COLOR_END + ".");
	}

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
		
		for (server s : server.getServers())
		{
			if (s.isServices() == false && s.getSplit() == null)
			{
				moo.sock.write("STATS u " + s.getName());
				message242.waiting_for.add(s.getName());
			}
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
	
	public static void post_update(String source, String target)
	{
		Date highest = null, lowest = null;
		split highest_sp = null, lowest_sp = null;
		Date now = new Date();
		
		for (server s : server.getServers())
		{
			if (s.isServices() || s.uptime == null)
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
		
		boolean shown = false;
		for (server s : server.getServers())
		{
			if (s.isServices() || s.uptime == null)
				continue;
			else if (want_server != null && moo.matches(s.getName(), "*" + want_server + "*") == false)
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
				buffer += moo.difference(now, sp.when);
				buffer += message.COLOR_END;
			}
			
			if (commandUptime.only_extremes)
			{
				if (is_extreme)
				{
					moo.reply(source, target, buffer);
					shown = true;
				}
			}
			else
			{
				moo.reply(source, target, buffer);
				shown = true;
			}
		}
		
		if (shown == false)
		{
			if (want_server != null)
				moo.reply(source, target, "No servers match " + want_server);
			else
				moo.reply(source, target, "You have managed to execute a command with no servers on the network, congrats");
		}
	}
}
