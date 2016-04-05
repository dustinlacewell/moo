package net.rizon.moo.plugin.logging;

import com.google.inject.Inject;
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
import net.rizon.moo.CommandSource;
import net.rizon.moo.plugin.logging.conf.LoggingConfiguration;
import net.rizon.moo.util.Match;

class LogSearcher extends Thread
{
	private CommandSource source;
	private String search;
	private int limit;
	private int days;
	
	private LoggingConfiguration conf;

	@Inject
	public LogSearcher(LoggingConfiguration conf)
	{
		this.conf = conf;
	}

	public void setSource(CommandSource source)
	{
		this.source = source;
	}

	public void setSearch(String search)
	{
		this.search = search;
	}

	public void setLimit(int limit)
	{
		this.limit = limit;
	}

	public void setDays(int days)
	{
		this.days = days;
	}

	@Override
	public void run()
	{
		Deque<String> matches = new ArrayDeque<>();
		int nummatches = 0;
		
		for (int i = this.days - 1; i >= 0; --i)
		{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -i);
			Date then = cal.getTime();

			DateFormat format = new SimpleDateFormat(conf.date);
			File logPath = new File(conf.path);
			File logFilePath = new File(logPath, conf.filename.replace("%DATE%", format.format(then)));
			
			if (!logFilePath.exists())
				continue;
			
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFilePath))))
			{
				for (String line; (line = reader.readLine()) != null;)
					if (Match.matches(line, search))
					{
						++nummatches;
						matches.addLast(line);

						if (limit > 0 && matches.size() >= limit)
						{
							matches.pop();
						}
					}
			}
			catch (Exception e)
			{
				CommandSLogSearch.logger.warn("Unable to search logfile", e);
			}
		}
		
		for (String match : matches)
			source.notice(match);

		source.reply("Done, " + matches.size() + "/" + nummatches + " shown.");
	}
}