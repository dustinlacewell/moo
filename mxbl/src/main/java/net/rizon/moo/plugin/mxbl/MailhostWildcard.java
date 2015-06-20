package net.rizon.moo.plugin.mxbl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;

/**
 *
 * @author Orillion <orillion@rizon.net>
 */
public class MailhostWildcard
{
	private static final Logger log = Logger.getLogger(MailhostWildcard.class.getName());
	private static final HashMap<MailhostWildcard, HashSet<Mailhost>> ownerMap = new HashMap<MailhostWildcard, HashSet<Mailhost>>();
	private static final HashMap<String, MailhostWildcard> wildcardMap = new HashMap<String, MailhostWildcard>();
	private final String wildcard;
	private final String oper;
	private final Date created;
	private int id;

	public static MailhostWildcard getMailhostWildcard(String mailhost)
	{
		return wildcardMap.get(mailhost);
	}

	public static Collection<MailhostWildcard> getMailhostWildcards()
	{
		return wildcardMap.values();
	}

	public HashSet<Mailhost> getMailhosts()
	{
		return ownerMap.get(this);
	}

	public String getWildcard()
	{
		return this.wildcard;
	}

	@SuppressWarnings("LeakingThisInConstructor")
	public MailhostWildcard(ResultSet rs) throws SQLException
	{
		this.id = rs.getInt("id");
		this.wildcard = rs.getString("wildcard");
		this.oper = rs.getString("oper");
		this.created = rs.getDate("created");
		HashSet<Mailhost> set = new HashSet<Mailhost>();
		ownerMap.put(this, set);
		wildcardMap.put(this.wildcard, this);
	}

	@SuppressWarnings("LeakingThisInConstructor")
	public MailhostWildcard(String wildcard, String oper)
	{
		this.id = -1;
		this.wildcard = wildcard;
		this.oper = oper;
		this.created = new Date();
		HashSet<Mailhost> set = new HashSet<Mailhost>();
		ownerMap.put(this, set);
		wildcardMap.put(this.wildcard, this);
	}

	public void addHost(Mailhost m)
	{
		HashSet<Mailhost> set = ownerMap.get(this);
		set.add(m);
	}

	public void removeHost(Mailhost m)
	{
		HashSet<Mailhost> set = ownerMap.get(this);
		set.remove(m);
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof MailhostWildcard)
		{
			return this.hashCode() == other.hashCode();
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		int hash = 5;
		hash = 61 * hash + (this.wildcard != null ? this.wildcard.hashCode() : 0);
		hash = 61 * hash + (this.oper != null ? this.oper.hashCode() : 0);
		hash = 61 * hash + (this.created != null ? this.created.hashCode() : 0);
		return hash;
	}

	@Override
	public String toString()
	{
		return "\2" + this.wildcard + "\2 set by " + this.oper + " on " + this.created.toString();
	}

	public int getId()
	{
		return this.id;
	}

	public void insert()
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO `mxbl_wildcard`"
					+ "(`wildcard`, `oper`, `created`)"
					+ "VALUES (?, ?, ?)");
			stmt.setString(1, this.wildcard);
			stmt.setString(2, this.oper);
			stmt.setDate(3, new java.sql.Date(this.created.getTime()));
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next())
			{
				this.id = rs.getInt(1);
				rs.close();
				stmt.close();
			}
		}
		catch (SQLException ex)
		{
			log.log(ex);
		}
	}

	public void unblock()
	{
		for (Mailhost m : this.getMailhosts())
		{
			m.unblock(false);
		}
		try
		{
			PreparedStatement stmt = Moo.db.prepare("DELETE FROM `mxbl_wildcard` WHERE `id` = ?");
			stmt.setInt(1, this.id);
			stmt.executeUpdate();
			ownerMap.remove(this);
			wildcardMap.remove(this.wildcard);
		}
		catch (SQLException ex)
		{
			log.log(ex);
		}
	}

}
