package net.rizon.moo.vote;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;

import net.rizon.moo.database;
import net.rizon.moo.moo;

class voteinfo
{
	public int id;
	public String channel, info, owner;
	public Date date = new Date();
	public boolean closed = false;
	
	public void insert()
	{
		try
		{
			PreparedStatement stmt = moo.db.prepare("INSERT INTO `votes` (`id`, `channel`, `info`, `owner`, `date`, `closed`) VALUES(?, ?, ?, ?, ?, ?)");
			stmt.setInt(1, this.id);
			stmt.setString(2, this.channel);
			stmt.setString(3, this.info);
			stmt.setString(4, this.owner);
			stmt.setDate(5, new java.sql.Date(this.date.getTime()));
			stmt.setBoolean(6, this.closed);
			
			moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			database.handleException(ex);
		}
	}
	
	public void close()
	{
		try
		{
			PreparedStatement stmt = moo.db.prepare("UPDATE `votes` SET `closed` = 1 WHERE `id` = ? AND `channel` = ?");
			stmt.setInt(1, this.id);
			stmt.setString(2, this.channel);
			moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			database.handleException(ex);
		}
	}
	
	public boolean findCastFor(final String nick)
	{
		cast[] casts = cast.getCastsFor(this);
		if (casts != null)
			for (final cast c : casts)
				if (c.voter.equalsIgnoreCase(nick))
					return true;
		return false;
	}
	
	public static int getMaxFor(final String chan)
	{
		try
		{
			PreparedStatement stmt = moo.db.prepare("SELECT MAX(`id`) as max FROM votes WHERE channel = ?");
			stmt.setString(1, chan);
			ResultSet rs = moo.db.executeQuery();

			int id = 1;
			if (rs.next())
				id = rs.getInt("max") + 1;
			
			return id;
		}
		catch (SQLException ex)
		{
			database.handleException(ex);
			return -1;
		}
	}
	
	public static voteinfo getVote(int id, final String channel)
	{
		try
		{
			PreparedStatement stmt = moo.db.prepare("SELECT * FROM `votes` WHERE `id` = ? AND `channel` = ?");
			stmt.setInt(1, id);
			stmt.setString(2, channel);
			
			ResultSet rs = moo.db.executeQuery();
			if (rs.next())
			{
				voteinfo vi = new voteinfo();
				vi.id = id;
				vi.channel = rs.getString("channel");
				vi.info = rs.getString("info");
				vi.owner = rs.getString("owner");
				vi.date = rs.getDate("date");
				vi.closed = rs.getBoolean("closed");
				return vi;
			}
		}
		catch (SQLException ex)
		{
			database.handleException(ex);
		}
		
		return null;
	}
	
	public static voteinfo[] getVotes(final String channel)
	{
		try
		{
			PreparedStatement stmt = moo.db.prepare("SELECT * FROM `votes` WHERE `channel` = ?");
			stmt.setString(1, channel);
			
			ResultSet rs = moo.db.executeQuery();
			LinkedList<voteinfo> vis = new LinkedList<voteinfo>();
			while (rs.next())
			{
				voteinfo vi = new voteinfo();
				vi.id = rs.getInt("id");
				vi.channel = rs.getString("channel");
				vi.info = rs.getString("info");
				vi.owner = rs.getString("owner");
				vi.date = rs.getDate("date");
				vi.closed = rs.getBoolean("closed");
				
				vis.add(vi);
			}
			
			voteinfo[] votes = new voteinfo[vis.size()];
			vis.toArray(votes);
			return votes;
		}
		catch (SQLException ex)
		{
			database.handleException(ex);
		}
		
		return null;
	}
}
