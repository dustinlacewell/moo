package net.rizon.moo.vote;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import net.rizon.moo.database;
import net.rizon.moo.moo;

class cast
{
	public int id;
	public String channel, voter;
	public boolean vote;
	
	public void insert()
	{
		try
		{
			PreparedStatement stmt = moo.db.prepare("INSERT INTO `vote_casts` (`id`, `channel`, `voter`, `vote`) VALUES(?, ?, ?, ?)");
			stmt.setInt(1, this.id);
			stmt.setString(2, this.channel);
			stmt.setString(3, this.voter);
			stmt.setBoolean(4, this.vote);
			moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			database.handleException(ex);
		}
	}
	
	public static cast[] getCastsFor(voteinfo vote)
	{
		try
		{
			PreparedStatement stmt = moo.db.prepare("SELECT * FROM `vote_casts` WHERE `id` = ? AND `channel` = ?");
			stmt.setInt(1, vote.id);
			stmt.setString(2, vote.channel);
			
			ResultSet rs = moo.db.executeQuery();
			LinkedList<cast> casts = new LinkedList<cast>();
			while (rs.next())
			{
				cast c = new cast();
				c.id = rs.getInt("id");
				c.channel = rs.getString("channel");
				c.voter = rs.getString("voter");
				c.vote = rs.getBoolean("vote");
				casts.add(c);
			}
			
			cast[] c = new cast[casts.size()];
			casts.toArray(c);
			return c;
		}
		catch (SQLException ex)
		{
			database.handleException(ex);
		}
		
		return null;
	}
}