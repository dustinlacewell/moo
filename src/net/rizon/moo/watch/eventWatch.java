package net.rizon.moo.watch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;

import net.rizon.moo.event;
import net.rizon.moo.moo;

public class eventWatch extends event
{
	@Override
	protected void initDatabases()
	{
		moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `watches` (`nick` varchar(64), `creator` varchar(64), `reason` varchar(64), `created` date, `expires` date);");
	}
	
	@Override
	public void loadDatabases()
	{
		try
		{
			ResultSet rs = moo.db.executeQuery("SELECT * FROM `watches`");
			while (rs.next())
			{
				watchEntry we = new watchEntry();
				
				we.nick = rs.getString("nick");
				we.creator = rs.getString("creator");
				we.reason = rs.getString("reason");
				we.created = new Date(rs.getDate("created").getTime());
				we.expires = new Date(rs.getDate("expires").getTime());
				
				watch.watches.add(we);
			}
		}
		catch (Exception ex)
		{
			System.out.println("Error loading watches");
			ex.printStackTrace();
		}
	}
	
	@Override
	public void saveDatabases()
	{
		try
		{
			moo.db.executeUpdate("DELETE FROM `watches`");
			
			PreparedStatement statement = moo.db.prepare("INSERT INTO `watches` (`nick`, `creator`, `reason`, `created`, `expires`) VALUES(?, ?, ?, ?, ?);");
			
			for (Iterator<watchEntry> it = watch.watches.iterator(); it.hasNext();)
			{
				watchEntry e = it.next();
				
				statement.setString(1, e.nick);
				statement.setString(2, e.creator);
				statement.setString(3, e.reason);
				statement.setDate(4, new java.sql.Date(e.created.getTime()));
				statement.setDate(5, new java.sql.Date(e.expires.getTime()));
				
				moo.db.executeUpdate();
			}
		}
		catch (SQLException ex)
		{
			System.out.println("Error saving watches");
			ex.printStackTrace();
		}
	}
}
