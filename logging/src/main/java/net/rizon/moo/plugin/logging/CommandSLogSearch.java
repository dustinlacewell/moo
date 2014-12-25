package net.rizon.moo.plugin.logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.logging.Level;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

class logSearcher extends Thread
{
	private CommandSource source;
	private String search;
	private int limit;

	public logSearcher(CommandSource source, final String search, final int limit)
	{
		this.source = source;
		this.search = search;
		this.limit = limit;
	}

	@Override
	public void run()
	{
		Deque<String> matches = new ArrayDeque<String>();
		int nummatches = 0;
		
		for (int i = logging.conf.searchDays - 1; i >= 0; --i)
		{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -i);
			Date then = cal.getTime();

			DateFormat format = new SimpleDateFormat(logging.conf.date);
			File logPath = new File(logging.conf.path);
			File logFilePath = new File(logPath, logging.conf.filename.replace("%DATE%", format.format(then)));
			
			if (!logFilePath.exists())
				continue;
			
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFilePath)));
				try
				{
					for (String line; (line = reader.readLine()) != null;)
						if (Moo.matches(line, "*" + search + "*"))
						{
							++nummatches;
							matches.addLast(line);
							
							if (limit > 0 && matches.size() > limit)
								matches.pop();
						}
				}
				finally
				{
					reader.close();
				}
			}
			catch (Exception e)
			{
				logging.log.log(Level.WARNING, "Unable to search logfile", e);
			}
		}
		
		for (String match : matches)
			source.notice(match);

		source.reply("Done, " + matches.size() + "/" + nummatches + " shown.");
	}
}

class CommandSLogSearch extends Command
{
	public CommandSLogSearch(Plugin pkg)
	{
		super(pkg, "!SLOGSEARCH", "Search through services logs");
		this.requiresChannel(Moo.conf.oper_channels);
		this.requiresChannel(Moo.conf.admin_channels);
	}
	
	private static final int defaultLimit = 100;

	@Override
	public void execute(CommandSource source, final String[] params)
	{
		if (params.length <= 1)
			return;

		int limit = defaultLimit;
		if (params.length >= 3)
			try
			{
				limit = Integer.parseInt(params[2]);
				if (limit <= 0)
					return;
			}
			catch (NumberFormatException ex) { }

		new logSearcher(source, params[1], limit).start();
	}
}