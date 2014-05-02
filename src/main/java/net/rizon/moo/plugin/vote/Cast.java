package net.rizon.moo.plugin.vote;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import net.rizon.moo.Database;
import net.rizon.moo.Moo;

class Cast
{
	public int id;
	public String channel, voter;
	public boolean vote;
	
	public void insert()
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO `vote_casts` (`id`, `channel`, `voter`, `vote`) VALUES(?, ?, ?, ?)");
			stmt.setInt(1, this.id);
			stmt.setString(2, this.channel);
			stmt.setString(3, this.voter);
			stmt.setBoolean(4, this.vote);
			Moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
		}
	}
	
	public static Cast[] getCastsFor(VoteInfo vote)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("SELECT * FROM `vote_casts` WHERE `id` = ? AND `channel` = ?");
			stmt.setInt(1, vote.id);
			stmt.setString(2, vote.channel);
			
			ResultSet rs = Moo.db.executeQuery();
			LinkedList<Cast> casts = new LinkedList<Cast>();
			while (rs.next())
			{
				Cast c = new Cast();
				c.id = rs.getInt("id");
				c.channel = rs.getString("channel");
				c.voter = rs.getString("voter");
				c.vote = rs.getBoolean("vote");
				casts.add(c);
			}
			
			Cast[] c = new Cast[casts.size()];
			casts.toArray(c);
			return c;
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
		}
		
		return null;
	}
}