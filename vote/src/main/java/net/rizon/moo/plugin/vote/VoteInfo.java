package net.rizon.moo.plugin.vote;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;

import net.rizon.moo.Database;
import net.rizon.moo.Moo;

class VoteInfo
{
	public int id;
	public String channel, info, owner;
	public Date date = new Date();
	public boolean closed = false;

	public void insert()
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO `votes` (`id`, `channel`, `info`, `owner`, `date`, `closed`) VALUES(?, ?, ?, ?, ?, ?)");
			stmt.setInt(1, this.id);
			stmt.setString(2, this.channel);
			stmt.setString(3, this.info);
			stmt.setString(4, this.owner);
			stmt.setDate(5, new java.sql.Date(this.date.getTime()));
			stmt.setBoolean(6, this.closed);

			Moo.db.executeUpdate(stmt);
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
		}
	}

	public void close()
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("UPDATE `votes` SET `closed` = 1 WHERE `id` = ? AND `channel` = ?");
			stmt.setInt(1, this.id);
			stmt.setString(2, this.channel);
			Moo.db.executeUpdate(stmt);
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
		}
	}

	public boolean findCastFor(final String nick)
	{
		Cast[] casts = Cast.getCastsFor(this);
		if (casts != null)
			for (final Cast c : casts)
				if (c.voter.equalsIgnoreCase(nick))
					return true;
		return false;
	}

	public static int getMaxFor(final String chan)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("SELECT MAX(`id`) as max FROM votes WHERE channel = ?");
			stmt.setString(1, chan);
			ResultSet rs = Moo.db.executeQuery(stmt);

			int id = 1;
			if (rs.next())
				id = rs.getInt("max") + 1;
			
			rs.close();
			stmt.close();

			return id;
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
			return -1;
		}
	}

	public static VoteInfo getVote(int id, final String channel)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("SELECT * FROM `votes` WHERE `id` = ? AND `channel` = ?");
			stmt.setInt(1, id);
			stmt.setString(2, channel);

			ResultSet rs = Moo.db.executeQuery(stmt);
			if (rs.next())
			{
				VoteInfo vi = new VoteInfo();
				vi.id = id;
				vi.channel = rs.getString("channel");
				vi.info = rs.getString("info");
				vi.owner = rs.getString("owner");
				vi.date = rs.getDate("date");
				vi.closed = rs.getBoolean("closed");
				rs.close();
				stmt.close();
				return vi;
			}
			
			rs.close();
			stmt.close();
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
		}

		return null;
	}

	public static VoteInfo[] getVotes(final String channel)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("SELECT * FROM `votes` WHERE `channel` = ?");
			stmt.setString(1, channel);

			ResultSet rs = Moo.db.executeQuery(stmt);
			LinkedList<VoteInfo> vis = new LinkedList<VoteInfo>();
			while (rs.next())
			{
				VoteInfo vi = new VoteInfo();
				vi.id = rs.getInt("id");
				vi.channel = rs.getString("channel");
				vi.info = rs.getString("info");
				vi.owner = rs.getString("owner");
				vi.date = rs.getDate("date");
				vi.closed = rs.getBoolean("closed");

				vis.add(vi);
			}
			
			rs.close();
			stmt.close();

			VoteInfo[] votes = new VoteInfo[vis.size()];
			vis.toArray(votes);
			return votes;
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
		}

		return null;
	}
}
