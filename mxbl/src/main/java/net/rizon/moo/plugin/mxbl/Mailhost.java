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
	public static final HashMap<String, Mailhost> mailhosts = new HashMap<String, Mailhost>();
	public final String mailhost;
	public final String oper;
	public final Date date;

	public static Mailhost getMailhost(String mailhost)
	{
		return mailhosts.get(mailhost.toLowerCase());
	}

	public static Collection<Mailhost> getMailhosts()
	{
		return mailhosts.values();
	}

	@SuppressWarnings("LeakingThisInConstructor")
	public Mailhost(String mailhost, String oper)
	{
		this.mailhost = mailhost.toLowerCase();
		this.oper = oper;
		this.date = new Date();
		mailhosts.put(mailhost.toLowerCase(), this);
	}

	@SuppressWarnings("LeakingThisInConstructor")
	public Mailhost(ResultSet rs) throws SQLException
	{
		this.mailhost = rs.getString("host");
		this.oper = rs.getString("oper");
		this.date = rs.getDate("created");
		mailhosts.put(this.mailhost, this);
	}

	public void addIP(String ip)
	{
		new MailIP(ip, this);
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
		return "\2" + mailhost + "\2 set by " + oper + " on " + date.toString();
	}

	/**
	 * Inserts this mailhost into the database.
	 */
	public void insert()
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO `mxbl` (`host`, `oper`, `created`) VALUES(?, ?, ?)");
			stmt.setString(1, this.mailhost);
			stmt.setString(2, this.oper);
			stmt.setDate(3, new java.sql.Date(this.date.getTime()));
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

	public void unblock()
	{
		MailIP.delete(this);
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

		Mailhost.mailhosts.remove(this.mailhost.toLowerCase());
	}
}
