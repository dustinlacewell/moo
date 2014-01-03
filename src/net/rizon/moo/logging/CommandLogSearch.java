package net.rizon.moo.logging;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.rizon.moo.Command;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

class CommandLogSearch extends Command
{
	public CommandLogSearch(Plugin pkg)
	{
		super(pkg, "!LOGSEARCH", "Search through server logs"); 
		
		this.requiresChannel(Moo.conf.getList("staff_channels"));
		this.requiresChannel(Moo.conf.getList("oper_channels"));
		this.requiresChannel(Moo.conf.getList("admin_channels"));
	}
	
	@Override
	public void onHelp(String source)
	{
		Moo.notice(source, "Syntax: !LOGSEARCH <target> [limit]");
		Moo.notice(source, "Searches through moo's logs, finding all actions that affected the given target.");
		Moo.notice(source, "by default, only 10 items will be shown, unless there is a limit specifying otherwise.");
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
		
		Moo.reply(source, target, "Searching for the last " + limit + " events for " + what);
		
		try
		{
			PreparedStatement stmt = Moo.db.prepare("SELECT * FROM log WHERE `target` = ? ORDER BY `created` DESC");
			stmt.setString(1, what);
			
			ResultSet rs = Moo.db.executeQuery();
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
							Moo.reply(source, target, "#" + count + " on " + d + " - " + type + " - By " + lsource + " on " + ltarget + " - Reason: " + reason);
						else
							Moo.reply(source, target, "#" + count + " on " + d + " - " + type + " - By " + lsource + " on " + ltarget);
					}
					else
						Moo.reply(source, target, "#" + count + " on " + d + " - " + type + " - For " + ltarget);
				}
				
			}
			
			Moo.reply(source, target, "Done, " + shown + "/" + count + " shown");
		}
		catch (SQLException ex)
		{
			Moo.reply(source, target, "Error processing request");
			Logger.getGlobalLogger().log(ex);
		}
	}
}