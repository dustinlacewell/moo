package net.rizon.moo.plugin.logging;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

class CommandLogSearch extends Command
{
	public CommandLogSearch(Plugin pkg)
	{
		super(pkg, "!LOGSEARCH", "Search through server logs");

		this.requiresChannel(Moo.conf.staff_channels);
		this.requiresChannel(Moo.conf.oper_channels);
		this.requiresChannel(Moo.conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !LOGSEARCH { <target> | <(source|type|target|reason)=searcharg> ... } [limit]");
		source.notice("Searches through moo's logs, finding all actions that affected the given target.");
		source.notice("by default, only 10 items will be shown, unless there is a limit specifying otherwise.");
		source.notice("Replies for a limit greater than 10 will be given via notice.");
		source.notice(" ");
		source.notice("If given, one or more of source, type, target or reason can be searched for; else");
		source.notice("the given search term will be applied against the target field (e.g., target in a");
		source.notice("KILL or OLINE log type entry).");
		source.notice("When searching for the reason field, you may only search for one keyword.");
		source.notice(" ");
		source.notice("The following type arguments are currently supported:");
		source.notice(" AKILL, AKILLDEL, CLINE, CONNECT, KILL, LINK, OLINE, OPER, SPLIT");
		source.notice(" ");
		source.notice("Example: !LOGSEARCH moo -- searches for all entries for which \"moo\" was the");
		source.notice(" target. Expect to see a lot of OPER events.");
		source.notice("Example: !LOGSEARCH source=culex 100 -- searches for log entries for which");
		source.notice(" \"culex\" was the source and displays 100 instead of 10 items.");
		source.notice("Example: !LOGSEARCH type=OLINE target=wof -- searches for all O:line-related");
		source.notice(" events concerning \"wof\".");
	}

	private final void replyWithLimit(CommandSource source, int limit, String buffer)
	{
		if (limit > 10)
			source.notice(buffer);
		else
			source.reply(buffer);
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length < 2)
			return;

		final String what = params[1];
		int limit = 10;
		boolean hasExplicitLimit = false;
		if (params.length > 2)
		{
			try
			{
				limit = Integer.parseInt(params[params.length - 1]);
				hasExplicitLimit = true;
			} catch (Exception ex) { }
		}

		final StringBuffer searchMessage = new StringBuffer();
		final StringBuffer queryBuffer = new StringBuffer("SELECT * FROM log WHERE ");
		final ArrayList<String> arguments = new ArrayList<String>(params.length - 1);
		for (int i = 1; i < params.length; i++)
		{
			if (!(hasExplicitLimit && i == params.length - 1))
			{
				searchMessage.append(params[i]);
				if (i < params.length - (hasExplicitLimit ? 2 : 1))
					searchMessage.append(", ");
			}

			final String[] splitArgument = params[i].split("=");
			if (splitArgument.length < 2 || splitArgument[0].isEmpty())
			{
				if (arguments.size() > 0 && i != params.length - 1)
				{
					source.reply("Argument " + params[i] + " not in column=argument format.");
					return;
				}

				/* Simple !LOGSEARCH target; abort and check below */
				break;
			}

			final String column = splitArgument[0].toLowerCase();
			String argument = splitArgument[1];

			if (column.equals("type"))
				argument = argument.toUpperCase();
			else if (column.equals("reason"))
				argument = "%" + argument + "%";

			if (!column.equals("type")
					&& !column.equals("source")
					&& !column.equals("target")
					&& !column.equals("reason"))
			{
				source.reply("Unknown column " + column + ".");
				return;
			}

			if (column.equals("reason"))
				queryBuffer.append("`" + column + "` LIKE ? AND");
			else
				queryBuffer.append("`" + column + "` = ? COLLATE NOCASE AND");

			arguments.add(argument);
		}

		if (arguments.size() == 0)
		{
			arguments.add(what);
			queryBuffer.append("`target` = ? COLLATE NOCASE AND");
		}

		queryBuffer.append(" 1=1 ORDER BY `created` DESC");

		replyWithLimit(source, limit, "Searching for the last " + limit + " events for " + searchMessage.toString());

		try
		{
			PreparedStatement stmt = Moo.db.prepare(queryBuffer.toString());
			int i = 1;
			for (final String argument : arguments)
				stmt.setString(i++, argument);

			ResultSet rs = Moo.db.executeQuery(stmt);
			int count = 0, shown = 0;
			while (rs.next())
			{
				String d = rs.getString("created");
				String type = rs.getString("type"), lsource = rs.getString("source"), ltarget = rs.getString("target"), reason = rs.getString("reason");

				++count;

				if (limit > 0)
				{
					--limit;
					++shown;

					if (lsource != null && lsource.isEmpty() == false)
					{
						if (reason != null && reason.isEmpty() == false)
							replyWithLimit(source, limit, "#" + count + " on " + d + " - " + type + " - By " + lsource + " on " + ltarget + " - Reason: " + reason);
						else
							replyWithLimit(source, limit, "#" + count + " on " + d + " - " + type + " - By " + lsource + " on " + ltarget);
					}
					else
						replyWithLimit(source, limit, "#" + count + " on " + d + " - " + type + " - For " + ltarget);
				}

			}
			
			rs.close();
			stmt.close();

			replyWithLimit(source, limit, "Done, " + shown + "/" + count + " shown");
		}
		catch (SQLException ex)
		{
			source.reply("Error processing request");
			Logger.getGlobalLogger().log(ex);
		}
	}
}