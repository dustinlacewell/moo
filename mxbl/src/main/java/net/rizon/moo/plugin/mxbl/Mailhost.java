package net.rizon.moo.plugin.mxbl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;

/**
 *
 * @author Orillion <orillion@rizon.net>
 */
public class Mailhost
{
	private static final Logger log = Logger.getLogger(Mailhost.class.getName());
	private static final HashMap<String, Mailhost> mailhosts = new HashMap<String, Mailhost>();
	public final String mailhost;
	public final String oper;
	public final Date date;
	private final MailhostWildcard owner;

	public static Mailhost getMailhost(String mailhost)
	{
		return mailhosts.get(mailhost.toLowerCase());
	}

	public static Collection<Mailhost> getMailhosts()
	{
		return mailhosts.values();
	}

	@SuppressWarnings("LeakingThisInConstructor")
	public Mailhost(String mailhost, String oper, Date date, MailhostWildcard owner)
	{
		this.mailhost = mailhost.toLowerCase();
		this.oper = oper;
		this.date = date;
		this.owner = owner;
		mailhosts.put(mailhost.toLowerCase(), this);
	}

	@SuppressWarnings("LeakingThisInConstructor")
	public Mailhost(String mailhost, String oper, MailhostWildcard owner)
	{
		this.mailhost = mailhost.toLowerCase();
		this.oper = oper;
		this.date = new Date();
		this.owner = owner;
		mailhosts.put(mailhost.toLowerCase(), this);
	}

	@SuppressWarnings("LeakingThisInConstructor")
	public Mailhost(ResultSet rs, MailhostWildcard owner) throws SQLException
	{
		this.mailhost = rs.getString("host").toLowerCase();
		this.oper = rs.getString("oper");
		this.date = rs.getDate("created");
		this.owner = owner;
		mailhosts.put(this.mailhost, this);
	}

	public void addIP(String ip)
	{
		new MailIP(ip, this);
	}

	public MailhostWildcard getOwner()
	{
		return this.owner;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof Mailhost)
		{
			return this.hashCode() == other.hashCode();
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		int hash = 5;
		hash = 47 * hash + (this.mailhost != null ? this.mailhost.hashCode() : 0);
		hash = 47 * hash + (this.oper != null ? this.oper.hashCode() : 0);
		hash = 47 * hash + (this.date != null ? this.date.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString()
	{
		if (this.owner == null)
		{
			return "\2" + mailhost + "\2 set by " + oper + " on " + date.toString();
		}
		else
		{
			return "\2" + this.mailhost + "\2 matched against " + this.owner.toString();
		}
	}

	/**
	 * Inserts this mailhost into the database.
	 */
	public void insert()
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO `mxbl` (`wildcard_id`, `host`, `oper`, `created`) VALUES(?, ?, ?, ?)");
			if (this.owner == null)
			{
				stmt.setNull(1, java.sql.Types.INTEGER);
			}
			else
			{
				stmt.setInt(1, this.owner.getId());
			}
			stmt.setString(2, this.mailhost);
			stmt.setString(3, this.oper);
			stmt.setDate(4, new java.sql.Date(this.date.getTime()));
			Moo.db.executeUpdate(stmt);
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next())
			{
				int id = rs.getInt(1);
				rs.close();
				stmt.close();
				Moo.db.setAutoCommit(false);
				stmt = Moo.db.prepare("REPLACE INTO `mxbl_ips` (`host`, `ip`) VALUES(?, ?)");
				for (MailIP mailIP : MailIP.getMailIP(this))
				{
					stmt.setInt(1, id);
					stmt.setString(2, mailIP.ip);
					stmt.addBatch();
				}
				stmt.executeBatch();
				Moo.db.setAutoCommit(true);
			}

		}
		catch (SQLException ex)
		{
			log.log(ex);
		}
	}

	public void unblock(boolean sql)
	{
		MailIP.delete(this);
		if (sql)
		{
			try
			{
				PreparedStatement stmt = Moo.db.prepare("DELETE FROM `mxbl` WHERE `host` = ?");
				stmt.setString(1, this.mailhost);
				Moo.db.executeUpdate(stmt);
			}
			catch (SQLException ex)
			{
				log.log(ex);
			}
		}

		mailhosts.remove(this.mailhost, this);
	}
}
