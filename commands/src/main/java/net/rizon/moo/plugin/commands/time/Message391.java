package net.rizon.moo.plugin.commands.time;

import com.google.inject.Inject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Message;
import net.rizon.moo.conf.Config;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;
import org.slf4j.Logger;

public class Message391 extends Message
{
	@Inject
	private static Logger logger;

	@Inject
	private ServerManager serverManager;

	@Inject
	private Protocol protocol;

	@Inject
	private Config config;

	private int dashesFor(Server s)
	{
		int longest = 0;
		for (Server s2 : serverManager.getServers())
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

	public static Set<String> waiting_for = new HashSet<>();
	public static Map<Long, Integer> known_times = new HashMap<>();
	public static CommandSource command_source;
	public static boolean hourly_check = false;

	public Message391()
	{
		super("391");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (command_source == null && !hourly_check)
			return;

		Server s = serverManager.findServerAbsolute(message.getSource());
		if (s == null)
			return;

		if (waiting_for.remove(s.getName()) == false)
			return;

		try
		{
			String time_buf = message.getParams()[2];
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
				buf += message.getParams()[2] + Message.COLOR_END + " (off by " + (common_time - them) + " seconds)";
			}
			else
				buf += Message.COLOR_BRIGHTGREEN + message.getParams()[2] + Message.COLOR_END;

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
					for (String c : config.admin_channels)
						protocol.privmsg(c, buf);
				}
			}
			else
				command_source.reply(buf);
		}
		catch (ParseException ex)
		{
			logger.warn("Unable to parse time", ex);
		}
	}
}
