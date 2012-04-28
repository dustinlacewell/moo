package net.rizon.moo.commands;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

class message391 extends message
{
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
	
	private static long commonTime()
	{
		long highest_ts = 0;
		int highest_count = 0;

		for (Iterator<Long> it = known_times.keySet().iterator(); it.hasNext();)
		{
			long key = it.next();
			int i = known_times.get(key);
			
			if (i > highest_count)
			{
				highest_count = i;
				highest_ts = key;
			}
		}
		
		return highest_ts;
	}
	
	private static DateFormat format = new SimpleDateFormat("EEEE MMMM dd yyyy -- HH:mm:ss Z");
	
	public static HashSet<String> waiting_for = new HashSet<String>();
	public static HashMap<Long, Integer> known_times = new HashMap<Long, Integer>();
	public static String target_channel = null;
	public static String target_source = null;
	public static boolean hourly_check = false;

	public message391(String what)
	{
		super(what);
	}

	@Override
	public void run(String source, String[] msg)
	{
		if ((target_channel == null || target_source == null) && !hourly_check)
			return;

		server s = server.findServerAbsolute(source);
		if (s == null)
			return;
		
		if (waiting_for.remove(s.getName()) == false)
			return;
		
		try
		{
			String time_buf = msg[2];
			int co = time_buf.lastIndexOf(':');
			if (co == -1)
				return;
			time_buf = time_buf.substring(0, co) + time_buf.substring(co + 1);

			Date st = format.parse(time_buf);
			long them = st.getTime() / 1000L;
			
			String buf = "[TIME] " + s.getName() + " ";
			for (int i = 0, dashes = dashesFor(s); i < dashes; ++i)
				buf += "-";
			buf += " ";
			
			long common_time = commonTime();
			boolean time_is_very_off = false;
			
			if (common_time != 0 && common_time != them)
			{
				if (Math.abs(common_time - them) < 60)
					buf += message.COLOR_YELLOW;
				else
				{
					buf += message.COLOR_RED;
					time_is_very_off = true;
				}
				buf += msg[2] + message.COLOR_END + " (off by " + (common_time - them) + " seconds)";
			}
			else
				buf += message.COLOR_BRIGHTGREEN + msg[2] + message.COLOR_END;
			
			if (known_times.containsKey(them) == false)
				known_times.put(them, 1);
			else
			{
				int cur = known_times.get(them);
				known_times.put(them, cur + 1);
			}
			
			if (hourly_check)
			{
				if (time_is_very_off)
				{
					for (String c : moo.conf.getAdminChannels())
						moo.privmsg(c, buf);
				}
			}
			else
				moo.reply(target_source, target_channel, buf);
		}
		catch (ParseException ex)
		{
			ex.printStackTrace();
		}
	}
}

class checkTimesTimer extends timer
{
	public checkTimesTimer()
	{
		super(60, true);
	}
	
	@Override
	public void run(Date now)
	{
		message391.known_times.clear();
		message391.hourly_check = true;
		
		for (server s : server.getServers())
		{
			if (s.isServices())
				continue;
			moo.sock.write("TIME " + s.getName());
			message391.waiting_for.add(s.getName());
		}
	}
}

public class commandTime extends command
{
	@SuppressWarnings("unused")
	private static message391 message_391 = new message391("391");
	private checkTimesTimer check_times_timer;

	public commandTime(mpackage pkg)
	{
		super(pkg, "!TIME", "View server times");
		
		this.check_times_timer = new checkTimesTimer();
		this.check_times_timer.start();
	}

	@Override
	public void onHelp(String source)
	{
		moo.notice(source, "Syntax: !TIME");
		moo.notice(source, "Queries all IRCds about their current time and returns the responses.");
		moo.notice(source, "If there are significant differences in time between servers (at least");
		moo.notice(source, "60 seconds), the offset will be shown");
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		for (server s : server.getServers())
		{
			if (s.isServices())
				continue;
			moo.sock.write("TIME " + s.getName());
			message391.waiting_for.add(s.getName());
		}
		
		message391.known_times.clear();
		message391.hourly_check = false;
		message391.target_channel = target;
		message391.target_source = source;
	}
}