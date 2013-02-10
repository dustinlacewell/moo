package net.rizon.moo.logging;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

class commandSLogSearch extends command
{
	public commandSLogSearch(mpackage pkg)
	{
		super(pkg, "!SLOGSEARCH", "Search through services logs");
		this.requiresChannel(moo.conf.getAdminChannels());
	}

	@Override
	public void execute(String source, String target, final String[] params)
	{
		if (params.length <= 1)
			return;
		
		int num = 1000;
		if (params.length >= 3)
			try
			{
				num = Integer.parseInt(params[2]);
				if (num <= 0)
					return;
			}
			catch (NumberFormatException ex) { }
		
		try
		{
			PreparedStatement stmt = moo.db.prepare("SELECT max(id)+1 AS `max` FROM `services_logs`");
			ResultSet rs = moo.db.executeQuery();
			int max = rs.getInt("max");
			
			stmt = moo.db.prepare("SELECT `data` FROM `services_logs` WHERE `id` >= ? AND `data` LIKE ?");
			stmt.setInt(1, max - num);
			stmt.setString(2, "%" + params[1] + "%");
			rs = moo.db.executeQuery();
			
			int count = 0;
			while (rs.next())
			{
				++count;
				moo.reply(source, target, "MATCH: " + rs.getString("data"));
			}
			
			moo.reply(source, target, "Done, " + count + " shown.");
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
	}
}