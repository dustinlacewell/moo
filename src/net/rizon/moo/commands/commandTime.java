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

	public message391(String what)
	{
		super(what);
	}

	@Override
	public void run(String source, String[] message)
	{
		if (target_channel == null || target_source == null)
			return;

		server s = server.findServerAbsolute(source);
		if (s == null)
			return;
		
		if (waiting_for.remove(s.getName()) == false)
			return;
		
		try
		{
			String time_buf = message[2];
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
			
			if (common_time != 0 && common_time != them)
			{
				if (Math.abs(common_time - them) < 60)
					buf += "\00308";
				else
					buf += "\00304";
				buf += message[2] + "\003 (off by " + (common_time - them) + " seconds)";
			}
			else
				buf += "\00309" + message[2] + "\003";
			
			if (known_times.containsKey(them) == false)
				known_times.put(them, 1);
			else
			{
				int cur = known_times.get(them);
				known_times.put(them, cur + 1);
			}
			
			moo.sock.reply(target_source, target_channel, buf);
		}
		catch (ParseException ex)
		{
			ex.printStackTrace();
		}
	}
}

public class commandTime extends command
{
	@SuppressWarnings("unused")
	private static message391 message_391 = new message391("391");

	public commandTime(mpackage pkg)
	{
		super(pkg, "!TIME", "View server times"); 
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		for (server s : server.getServers())
		{
			moo.sock.write("TIME " + s.getName());
			message391.waiting_for.add(s.getName());
		}
		
		message391.known_times.clear();
		message391.target_channel = target;
		message391.target_source = source;
	}
}