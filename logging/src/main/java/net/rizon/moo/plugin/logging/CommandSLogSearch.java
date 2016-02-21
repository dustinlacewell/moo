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
import java.util.logging.Level;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.conf.Config;
import net.rizon.moo.plugin.logging.conf.LoggingConfiguration;
import net.rizon.moo.util.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class logSearcher extends Thread
{
	private CommandSource source;
	private String search;
	private int limit;
	private LoggingConfiguration conf;

	public logSearcher(CommandSource source, String search, int limit, LoggingConfiguration conf)
	{
		this.source = source;
		this.search = search;
		this.limit = limit;
		this.conf = conf;
	}

	@Override
	public void run()
	{
		Deque<String> matches = new ArrayDeque<String>();
		int nummatches = 0;
		
		for (int i = conf.searchDays - 1; i >= 0; --i)
		{
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -i);
			Date then = cal.getTime();

			DateFormat format = new SimpleDateFormat(conf.date);
			File logPath = new File(conf.path);
			File logFilePath = new File(logPath, conf.filename.replace("%DATE%", format.format(then)));
			
			if (!logFilePath.exists())
				continue;
			
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFilePath)));
				try
				{
					for (String line; (line = reader.readLine()) != null;)
						if (Match.matches(line, "*" + search + "*"))
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
				CommandSLogSearch.logger.warn("Unable to search logfile", e);
			}
		}
		
		for (String match : matches)
			source.notice(match);

		source.reply("Done, " + matches.size() + "/" + nummatches + " shown.");
	}
}

class CommandSLogSearch extends Command
{
	@Inject
	static Logger logger;

	@Inject
	private LoggingConfiguration conf;
	
	@Inject
	public CommandSLogSearch(Config conf)
	{
		super("!SLOGSEARCH", "Search through services logs");
		this.requiresChannel(conf.oper_channels);
		this.requiresChannel(conf.admin_channels);
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

		new logSearcher(source, params[1], limit, conf).start();
	}
}