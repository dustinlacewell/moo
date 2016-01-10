package net.rizon.moo.plugin.logging;

import com.google.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.conf.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CommandWLogSearch extends Command
{
	@Inject
	private static Logger logger;

	@Inject
	public CommandWLogSearch(Config conf)
	{
		super("!WLOGSEARCH", "Search through WALLOPS logs");
		
		this.requiresChannel(conf.staff_channels);
		this.requiresChannel(conf.oper_channels);
		this.requiresChannel(conf.admin_channels);
	}
	
	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: \002!WLOGSEARCH \u001F[+limit]\u001F \u001F<search terms>\u001F\002");
		source.notice(" ");
		source.notice("Searches through moo's WALLOPS logs, finding entries that match all search terms.");
		source.notice("by default, only 10 items will be shown, unless there is a limit specifying otherwise.");
		source.notice("NOTE: Replies for a limit greater than 10 will be given via notice.");
		source.notice(" ");
		source.notice("Examples:");
		source.notice(" ");
		source.notice("    \002!WLOGSEARCH +30 watch\002");
		source.notice("        Searches the WALLOPS log for the word 'watch' and");
		source.notice("        shows the 30 most recent ones (in notice)");
		source.notice(" ");
		source.notice("    \002!WLOGSEARCH cakes pie\002");
		source.notice("        Searches the WALLOPS log for the word 'cakes' and");
		source.notice("        'pie', and shows the 10 (default) most recent ones");
		source.notice("        (in channel)");
	}
	
	private void replyWithLimit(CommandSource source, int limit, String buffer)
	{
		if (limit > 10)
			source.notice(buffer);
		else
			source.reply(buffer);
	}
	
	@Override
	public void execute(CommandSource source, String[] params)
	{
		int limit = 10;
		if (params[1].charAt(0) == '+')
		{
			try
			{
				limit = Integer.parseInt(params[1].substring(1));
				String[] p = new String[params.length - 2];
				System.arraycopy(params, 2, p, 0, params.length - 2);
				params = p;
			}
			catch (Exception e)
			{
				source.notice("Syntax: !WLOGSEARCH [+limit] <search terms>");
				return;
			}
		}
		else
		{
			String[] p = new String[params.length - 1];
			System.arraycopy(params, 1, p, 0, params.length - 1);
			params = p;
		}
		
		final StringBuffer queryBuffer = new StringBuffer("SELECT * FROM wallops_logs WHERE ");
		
		for (int i = 0; i < params.length; i++)
		{
			if (i != 0)
				queryBuffer.append(" AND ");
			
			queryBuffer.append("`message` LIKE ?");
		}
		
		try
		{
			PreparedStatement stmt = Moo.db.prepare(queryBuffer.toString());
			int i = 0;
			for (final String argument : params)
				stmt.setString(++i, "%" + argument + "%");
			
			ResultSet rs = Moo.db.executeQuery(stmt);
			int count = 0, shown = 0;
			while (rs.next())
			{
				String d = rs.getString("date");
				String type = rs.getString("type");
				String src = rs.getString("source");
				String message = rs.getString("message");
				
				++count;
				
				if (count <= limit)
				{
					++shown;
					
					if (src != null && !src.isEmpty())
					{
						replyWithLimit(source, limit, "#" + count + " on " + d + " - By " + src + " - " + type + " - " + message);
					}
				}
			}
			
			rs.close();
			stmt.close();

			replyWithLimit(source, limit, "Done, " + shown + "/" + count + " shown");
		}
		catch (SQLException ex)
		{
			source.reply("Error processing request");
			
			logger.warn("Error processing request", ex);
		}
	}
}
