package net.rizon.moo.logging;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

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
		
		int num = 50;
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
			PreparedStatement stmt = moo.db.prepare("SELECT `id` FROM `services_logs_indexes` WHERE `key` = ?");
			stmt.setString(1, params[1]);
			ResultSet rs = moo.db.executeQuery();
			
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			while (rs.next())
				indexes.add(rs.getInt("id"));
			
			int total = indexes.size();
			while (indexes.size() > num)
				indexes.remove(0);
			
			for (Iterator<Integer> it = indexes.iterator(); it.hasNext();)
			{
				stmt = moo.db.prepare("SELECT `data` FROM `services_logs` WHERE `id` = ?");
				stmt.setInt(1, it.next());
				rs = moo.db.executeQuery();
				
				moo.reply(source, target, "MATCH: " + rs.getString("data"));
			}
			
			moo.reply(source, target, "Done. " + indexes.size() + "/" + total + " shown.");
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
	}
}