package net.rizon.moo.plugin.commands;

import io.netty.util.concurrent.ScheduledFuture;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Logger;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;

class message391 extends Message
{
	private static int dashesFor(Server s)
	{
		int longest = 0;
		for (Server s2 : Server.getServers())
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

	private static DateFormat format1 = new SimpleDateFormat("EEEE MMMM dd yyyy -- HH:mm:ss Z");
	private static DateFormat format2 = new SimpleDateFormat("EEEE MMMM dd yyyy -- HH:mm Z");

	public static HashSet<String> waiting_for = new HashSet<String>();
	public static HashMap<Long, Integer> known_times = new HashMap<Long, Integer>();
	public static CommandSource command_source;
	public static boolean hourly_check = false;

	public message391(String what)
	{
		super(what);
	}

	@Override
	public void run(String source, String[] msg)
	{
		if (command_source == null && !hourly_check)
			return;

		Server s = Server.findServerAbsolute(source);
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

			Date st;
			try
			{
				st = format1.parse(time_buf);
			}
			catch (ParseException ex)
			{
				st = format2.parse(time_buf);
			}
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
					buf += Message.COLOR_YELLOW;
				else
				{
					buf += Message.COLOR_RED;
					time_is_very_off = true;
				}
				buf += msg[2] + Message.COLOR_END + " (off by " + (common_time - them) + " seconds)";
			}
			else
				buf += Message.COLOR_BRIGHTGREEN + msg[2] + Message.COLOR_END;

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
					for (String c : Moo.conf.admin_channels)
						Moo.privmsg(c, buf);
				}
			}
			else
				command_source.reply(buf);
		}
		catch (ParseException ex)
		{
			Logger.getGlobalLogger().log(ex);
		}
	}
}

class CheckTimesTimer implements Runnable
{
	@Override
	public void run()
	{
		message391.known_times.clear();
		message391.hourly_check = true;

		for (Server s : Server.getServers())
		{
			if (s.isServices())
				continue;
			Moo.write("TIME", s.getName());
			message391.waiting_for.add(s.getName());
		}
	}
}

class CommandTime extends Command
{
	@SuppressWarnings("unused")
	private static message391 message_391 = new message391("391");
	private ScheduledFuture check_times_timer;

	public CommandTime(Plugin pkg)
	{
		super(pkg, "!TIME", "View server times");

		this.requiresChannel(Moo.conf.oper_channels);
		this.requiresChannel(Moo.conf.admin_channels);

		this.check_times_timer = Moo.scheduleWithFixedDelay(new CheckTimesTimer(), 15, TimeUnit.MINUTES);
	}
	
	public void remove()
	{
		this.check_times_timer.cancel(true);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !TIME");
		source.notice("Queries all IRCds about their current time and returns the responses.");
		source.notice("If there are significant differences in time between servers (at least");
		source.notice("60 seconds), the offset will be shown");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		for (Server s : Server.getServers())
		{
			if (s.isServices())
				continue;
			Moo.write("TIME", s.getName());
			message391.waiting_for.add(s.getName());
		}

		message391.known_times.clear();
		message391.hourly_check = false;
		message391.command_source = source;
	}
}