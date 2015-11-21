package net.rizon.moo.plugin.mxbl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.rizon.moo.Moo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Orillion <orillion@rizon.net>
 */
public class Mailhost
{
	private static final Logger logger = LoggerFactory.getLogger(Mailhost.class);

	private static final Map<String, Mailhost> mailhosts = new HashMap<String, Mailhost>();
	private static final Map<Integer, Mailhost> mailhostsIds = new HashMap<Integer, Mailhost>();

	private final List<MailIP> ips = new ArrayList<MailIP>();
	private final Set<Mailhost> childs = new HashSet<Mailhost>();
	public final String mailhost; // eg gmail.com (hostname mx records are associated with, or wildcard name?)
	public final String oper;
	public final Date created;
	private Mailhost owner;
	private int id;
	private final boolean isWildcard;
	//private List

	public static Mailhost getMailhost(String mailhost)
	{
		return mailhosts.get(mailhost.toLowerCase());
	}

	public static Mailhost getMailhost(int mailhost)
	{
		return mailhostsIds.get(mailhost);
	}

	public static Collection<Mailhost> getMailhosts()
	{
		return mailhosts.values();
	}

	public Collection<Mailhost> getChilds()
	{
		return childs;
	}

	public int getId()
	{
		return this.id;
	}

	@SuppressWarnings("LeakingThisInConstructor")
	public Mailhost(String mailhost, String oper, Date date, boolean wildcard, Mailhost owner)
	{
		this.id = -1;
		this.mailhost = mailhost.toLowerCase();
		this.oper = oper;
		this.isWildcard = wildcard;
		this.created = date;
		this.owner = owner;
		if (owner != null)
		{
			owner.addChild(this);
		}
		mailhosts.put(mailhost.toLowerCase(), this);
	}

	@SuppressWarnings("LeakingThisInConstructor")
	public Mailhost(String mailhost, String oper, boolean wildcard, Mailhost owner)
	{
		this.id = -1;
		this.mailhost = mailhost.toLowerCase();
		this.oper = oper;
		this.isWildcard = wildcard;
		this.created = new Date();
		this.owner = owner;
		if (owner != null)
		{
			owner.addChild(this);
		}
		mailhosts.put(mailhost.toLowerCase(), this);
	}

	@SuppressWarnings("LeakingThisInConstructor")
	public Mailhost(ResultSet rs, Mailhost owner) throws SQLException
	{
		this.id = rs.getInt("id");
		this.mailhost = rs.getString("host").toLowerCase();
		this.oper = rs.getString("oper");
		this.isWildcard = rs.getBoolean("wildcard");
		this.created = new Date(rs.getLong("created"));
		this.owner = owner;
		if (owner != null)
		{
			owner.addChild(this);
		}
		mailhostsIds.put(this.id, this);
		mailhosts.put(this.mailhost, this);
	}

	public List<MailIP> getIps()
	{
		return ips;
	}

	public void addIP(String ip)
	{
		ips.add(new MailIP(ip, this));
	}

	public Mailhost getOwner()
	{
		return this.owner;
	}

	public void setOwner(Mailhost owner)
	{
		this.owner = owner;
	}

	public void addChild(Mailhost child)
	{
		childs.add(child);
	}

	public void removeChild(Mailhost child)
	{
		childs.remove(child);
	}

	public boolean isWildcard()
	{
		return this.isWildcard;
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
		hash = 47 * hash + (this.created != null ? this.created.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString()
	{
		if (this.owner == null)
		{
			return "\2" + mailhost + "\2 set by " + oper + " on " + created.toString();
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
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO `mxbl` (`parent_id`, `host`, `oper`, `wildcard`, `created`) VALUES(?, ?, ?, ?, ?)");
			
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
			stmt.setBoolean(4, this.isWildcard);
			stmt.setLong(5, this.created.getTime());
			Moo.db.executeUpdate(stmt);
			
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next())
			{
				this.id = rs.getInt(1);
				mailhostsIds.put(this.id, this);

				Moo.db.setAutoCommit(false);
				PreparedStatement stmt2 = Moo.db.prepare("REPLACE INTO `mxbl_ips` (`host`, `ip`) VALUES(?, ?)");
				for (MailIP mailIP : this.getIps())
				{
					stmt2.setInt(1, this.id);
					stmt2.setString(2, mailIP.ip);
					stmt2.addBatch();
				}
				stmt2.executeBatch();
				Moo.db.setAutoCommit(true);
				
				stmt2.close();
			}
			
			rs.close();
			stmt.close();

		}
		catch (SQLException ex)
		{
			logger.info("SQL Exception: ", ex.getMessage());
		}
	}

	public void unblock()
	{
		for (Mailhost child : this.childs)
		{
			child.unblock();
		}

		try
		{
			PreparedStatement stmt = Moo.db.prepare("DELETE FROM `mxbl` WHERE `host` = ?");
			stmt.setString(1, this.mailhost);
			Moo.db.executeUpdate(stmt);
		}
		catch (SQLException ex)
		{
			logger.info("SQL Exception: ", ex.getMessage());
		}

		mailhosts.remove(this.mailhost);
		for (MailIP ip : ips)
		{
			MailIP.deleteIP(ip);
		}
	}
	
	public static boolean isInList(String ip)
	{
		return !getAllMailIP(ip).isEmpty();
	}

	public static List<MailIP> getAllMailIP(String ip)
	{
		List<MailIP> list = new ArrayList<MailIP>();
		for (Mailhost mh : mailhosts.values())
		{
			for (MailIP mailIP : mh.getIps())
			{
				if (mailIP.ip.equals(ip.trim()))
				{
					list.add(mailIP);
				}
			}
		}
		return list;
	}

//	public static void delete(MailIP ip)
//	{
//		ips.get(ip.getOwner()).remove(ip);
//	}
//
//	public static void delete(Mailhost m)
//	{
//		ips.remove(m);
//	}
}
