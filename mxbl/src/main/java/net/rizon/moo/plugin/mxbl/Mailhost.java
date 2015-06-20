package net.rizon.moo.plugin.mxbl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
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
	public List<MailIP> ips;

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
		this.ips = new ArrayList<MailIP>();
		mailhosts.put(mailhost.toLowerCase(), this);
	}

	@SuppressWarnings("LeakingThisInConstructor")
	public Mailhost(ResultSet rs) throws SQLException
	{
		this.mailhost = rs.getString("host");
		this.oper = rs.getString("oper");
		this.date = rs.getDate("created");
		this.ips = new ArrayList<MailIP>();
		mailhosts.put(this.mailhost, this);
	}

	public void addIP(String ip)
	{
		ips.add(new MailIP(ip, this));
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
				for (MailIP mailIP : ips)
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
		for (MailIP ip : ips)
		{
			MailIP.delete(ip);
		}
		try
		{
			PreparedStatement stmt = Moo.db.prepare("DELETE FROM `mxbl` WHERE `host` = ?");
			stmt.setString(1, this.mailhost);
			Moo.db.executeUpdate(stmt);
		}
		catch (SQLException ex)
		{
			Logger.getLogger(Mailhost.class.getName()).log(Level.SEVERE, null, ex);
		}

		Mailhost.mailhosts.remove(this.mailhost.toLowerCase());
	}
}
