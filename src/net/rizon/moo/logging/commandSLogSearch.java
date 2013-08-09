package net.rizon.moo.logging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.rizon.moo.command;
import net.rizon.moo.logger;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

class logSearcher extends Thread
{
	private String source, target;
	private String search;
	private int limit;
	
	public logSearcher(final String source, final String target, final String search, final int limit)
	{
		this.source = source;
		this.target = target;
		this.search = search;
		this.limit = limit;
	}
	
	@Override
	public void run()
	{
		Connection con = moo.db.getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = con.prepareStatement("SELECT max(id)+1 AS `max` FROM `services_logs`");
			rs = stmt.executeQuery();
			int max = rs.getInt("max");
			
			stmt.close();
			rs.close();
			
			if (this.limit > 0)
			{
				stmt = con.prepareStatement("SELECT `date`,`data` FROM `services_logs` WHERE `id` >= ? AND `data` LIKE ?");
				stmt.setInt(1, max - this.limit);
				stmt.setString(2, "%" + this.search + "%");
			}
			else
			{
				stmt = con.prepareStatement("SELECT `date`,`data` FROM `services_logs` WHERE `data` LIKE ?");
				stmt.setString(1, "%" + this.search + "%");
			}
			rs = stmt.executeQuery();
			
			int count = 0;
			while (rs.next())
			{
				++count;
				moo.reply(source, target, rs.getString("date") + ": " + rs.getString("data"));
			}
			
			if (this.limit > 0)
				moo.reply(source, target, "Done, " + count + " shown. Searched the last " + this.limit + " entries.");
			else
				moo.reply(source, target, "Done, " + count + " shown.");
		}
		catch (SQLException ex)
		{
			logger.getGlobalLogger().log(ex);
		}
		finally
		{
			try { stmt.close(); } catch (Exception ex) { }
			try { rs.close(); } catch (Exception ex) { }
		}
	}
}

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
			if (params[2].equalsIgnoreCase("ALL"))
				num = 0;
			else
				try
				{
					num = Integer.parseInt(params[2]);
					if (num <= 0)
						return;
				}
				catch (NumberFormatException ex) { }
		
		new logSearcher(source, target, params[1], num).start();
	}
}