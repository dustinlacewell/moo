package net.rizon.moo.plugin.logging;

import com.google.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.conf.Config;
import net.rizon.moo.plugin.logging.conf.LoggingConfiguration;
import net.rizon.moo.util.ArgumentParser;
import net.rizon.moo.util.Match;
import org.slf4j.Logger;

class logSearcher extends Thread
{
	private CommandSource source;
	private String search;
	private int limit;
	private int days;
	@Inject
	private LoggingConfiguration conf;

	public logSearcher(CommandSource source, String search, int limit, int days)
	{
		this.source = source;
		this.search = search;
		this.limit = limit;
		this.days = days;
	}

	@Override
	public void run()
	{
		Deque<String> matches = new ArrayDeque<String>();
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

							if (limit > 0 && matches.size() >= limit)
							{
								matches.pop();
							}
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

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: \002!SLOGSEARCH \u001F[+limit]\u001F \u001F[+days]\u001F \u001F<search terms>\u001F\002");
		source.notice(" ");
		source.notice("Searches through moo's Services logs, finding entries that match all search terms.");
		source.notice("by default, only 30 items will be shown, unless there is a limit specifying otherwise.");
		source.notice("Valid days modifiers are d (days), w (weeks), m|M (months), y (years)");
		source.notice(" ");
		source.notice("Examples:");
		source.notice(" ");
		source.notice("    \002!SLOGSEARCH +10 +7w NickServ identified\002");
		source.notice("        Searches the last 7 weeks of logs for any entries");
		source.notice("        matching the words 'NickServ' and 'identified' and");
		source.notice("        shows a maximum of 10 results.");
		source.notice(" ");
		source.notice("    \002!SLOGSEARCH +1y DROP\002");
		source.notice("        Searches the last 1 year of logs for any entries");
		source.notice("        matching the words 'DROP' and shows a maximum of");
		source.notice("        100 (default) results.");
		source.notice(" ");
		source.notice("    \002!SLOGSEARCH +30 ChanServ @beef:*:\002");
		source.notice("        Searches the last " + conf.searchDays + " days (default) of logs for any");
		source.notice("        entries matching the words 'ChanServ' and '@beef:*:'");
		source.notice("        and shows a maximum of 30 results.");
	}

	private static final int defaultLimit = 100;

	@Override
	public void execute(CommandSource source, final String[] params)
	{
		if (params.length <= 1)
			return;

		int limit = defaultLimit;
		int days = conf.searchDays;
		int index = 1;

		if (params.length >= 3)
		{
			try
			{
				limit = Integer.parseInt(params[1]);
				if (limit <= 0)
					return;

				index = 2;
			}
			catch (NumberFormatException ex)
			{
				// Was not a limit argument.
			}

			try
			{
				days = ArgumentParser.parseTimeArgumentDays(params[1]);
				index = 2;
			}
			catch (IllegalArgumentException ex)
			{
				// Was not a days argument.
			}
		}

		if (params.length >= 4)
		{
			try
			{
				days = ArgumentParser.parseTimeArgumentDays(params[2]);
				index = 3;
			}
			catch (IllegalArgumentException ex)
			{
				// Was not a days argument.
			}
		}

		String[] args = Arrays.copyOfRange(params, index, params.length);

		new logSearcher(source, join("*", args), limit, days).start();
	}

	private String join(String what, String[] args)
	{
		StringBuilder sb = new StringBuilder();
		sb.append('*');
		for (String s : args)
		{
			sb.append(s);
			sb.append('*');
		}
		return sb.toString();
	}
}