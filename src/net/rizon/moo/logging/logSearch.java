package net.rizon.moo.logging;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

public class logSearch extends command
{
	public logSearch(mpackage pkg)
	{
		super(pkg, "!LOGSEARCH", "Search through server logs"); 
	}
	
	@Override
	public void onHelp(String source)
	{
		moo.notice(source, "Syntax: !LOGSEARCH <target> [limit]");
		moo.notice(source, "Searches through moo's logs, finding all actions that affected the given target.");
		moo.notice(source, "by default, only 10 items will be shown, unless there is a limit specifying otherwise.");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length < 2)
			return;
		
		final String what = params[1]; 
		int limit = 10;
		try
		{
			limit = Integer.parseInt(params[2]);
		}
		catch (Exception ex) { }
		
		moo.reply(source, target, "Searching for the last " + limit + " events for " + what);
		
		try
		{
			PreparedStatement stmt = moo.db.prepare("SELECT * FROM log WHERE `target` = ? ORDER BY `created` DESC");
			stmt.setString(1, what);
			
			ResultSet rs = moo.db.executeQuery();
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
						moo.reply(source, target, "#" + count + " on " + d + " - " + type + " - By " + lsource + " on " + ltarget + " - Reason: " + reason);
					else
						moo.reply(source, target, "#" + count + " on " + d + " - " + type + " - For " + ltarget);
				}
				
			}
			
			moo.reply(source, target, "Done, " + shown + "/" + count + " shown");
		}
		catch (SQLException ex)
		{
			moo.reply(source, target, "Error processing request");
			ex.printStackTrace();
		}
	}
}