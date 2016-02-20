package net.rizon.moo.irc;

import java.security.cert.X509Certificate;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import net.rizon.moo.Database;
import net.rizon.moo.Moo;
import net.rizon.moo.Split;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Server
{
	private static final Logger logger = LoggerFactory.getLogger(Server.class);


	private String name;
	private Date created;
	private String desc = "";
	private String sid = null;
	public HashSet<String> clines = new HashSet<String>(), clines_work = new HashSet<String>();
	// oper name -> flags
	public HashMap<String, String> olines, olines_work = new HashMap<String, String>();
	public Server uplink;
	public HashSet<Server> links = new HashSet<Server>();
	public long bytes = 0;
	public int users = 0, last_users = 0;
	public LinkedList<String> allowed_clines = new LinkedList<String>();
	public boolean frozen = false;
	/* from /stats u */
	public Date uptime;
	/* public cert for the ircd */
	public X509Certificate cert;
	/* current split */
	public Split split;

	public Server(final String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}

	public void setCreated(Date created)
	{
		this.created = created;
	}

	public Date getCreated()
	{
		return this.created;
	}

	public void setSID(final String s)
	{
		this.sid = s;
	}

	public String getSID()
	{
		return this.sid;
	}

	public boolean isHub()
	{
		return (this.getSID() != null && this.getSID().endsWith("H")) || this.getName().endsWith(".hub") || this.getName().startsWith("hub.");
	}

	public boolean isServices()
	{
		String sid = this.getSID();
		if (sid != null)
		{
			if (sid.endsWith("S") || sid.endsWith("PY"))
				return true;
			if (sid.endsWith("C") || sid.endsWith("H"))
				return false;
		}

		if (this.getName().startsWith("services."))
			return true;

		return false;
	}
	
	private boolean isJuped()
	{
		return uplink != null && (uplink.isServices() || uplink.isJuped());
	}

	public boolean isNormal()
	{
		return !isServices() && !isJuped() && getSplit() == null;
	}

	public void link(final Server to)
	{
		this.links.add(to);
	}

	public void split(Server from)
	{
		Date now = new Date();
		this.links.remove(from);

		Split s = new Split();
		s.me = this.getName();
		s.from = from.name;
		s.when = now;
		s.recursive = false;

		// Find servers that split from this one at the same time
//		for (Server serv : Server.getServers())
//		{
//			Split sp = serv.getSplit();
//			if (sp != null && sp.from.equals(this.name) && sp.when.getTime() / 1000L == now.getTime() / 1000L)
//			{
//				sp.recursive = true;
//
//				try
//				{
//					PreparedStatement statement = Moo.db.prepare("UPDATE splits SET `recursive` = ? WHERE `name` = ? AND `from` = ? AND `when` = ?");
//					statement.setBoolean(1, sp.recursive);
//					statement.setString(2, sp.me);
//					statement.setString(3, sp.from);
//					statement.setDate(4, new java.sql.Date(sp.when.getTime()));
//					Moo.db.executeUpdate(statement);
//				}
//				catch (SQLException ex)
//				{
//					Database.handleException(ex);
//				}
//			}
//		}

		try
		{
			PreparedStatement statement = Moo.db.prepare("INSERT INTO splits (`name`, `from`, `to`, `when`, `end`, `reconnectedBy`, `recursive`) VALUES(?, ?, ?, ?, ?, ?, ?)");
			statement.setString(1, s.me);
			statement.setString(2, s.from);
			statement.setString(3, s.to);
			statement.setDate(4, new java.sql.Date(s.when.getTime()));
			statement.setDate(5, (s.end != null ? new java.sql.Date(s.end.getTime()) : null));
			statement.setString(6, s.reconnectedBy);
			statement.setBoolean(7, s.recursive);
			Moo.db.executeUpdate(statement);
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
		}

		split = s;
	}

	public Split getSplit()
	{
		if (split != null)
			return split;

		if (!this.links.isEmpty())
			return null;

		try
		{
			PreparedStatement statement = Moo.db.prepare("SELECT * FROM `splits` WHERE `name` = ? ORDER BY `when` DESC LIMIT 1");
			statement.setString(1, this.getName());
			ResultSet rs = Moo.db.executeQuery(statement);
			if (rs.next())
			{
				String name = rs.getString("name"), from = rs.getString("from"), to = rs.getString("to"), rBy = rs.getString("reconnectedBy");
				Date when = rs.getDate("when"), end = rs.getDate("end");
				boolean recursive = rs.getBoolean("recursive");

				Split sp = new Split();
				sp.me = name;
				sp.from = from;
				sp.to = to;
				sp.when = when;
				sp.end = end;
				sp.reconnectedBy = rBy;
				sp.recursive = recursive;

				if (sp.end == null)
				{
					split = sp;
					return sp;
				}
			}
			
			rs.close();
			statement.close();
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
		}

		return null;
	}

	public Split[] getSplits()
	{
		try
		{
			LinkedList<Split> splits = new LinkedList<Split>();
			PreparedStatement stmt = Moo.db.prepare("SELECT * FROM `splits` WHERE `name` = ?  order by `when` asc");
			stmt.setString(1, this.getName());
			ResultSet rs = Moo.db.executeQuery(stmt);
			while (rs.next())
			{
				Split sp = new Split();
				sp.me = rs.getString("name");
				sp.from = rs.getString("from");
				sp.to = rs.getString("to");
				sp.when = rs.getDate("when");
				sp.end = rs.getDate("end");
				sp.reconnectedBy = rs.getString("reconnectedBy");
				splits.add(sp);
			}
			rs.close();
			stmt.close();

			// Most recent split is at the end
			Split[] s = new Split[splits.size()];
			splits.toArray(s);
			return s;
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
			return null;
		}
	}

	public void splitDel(final Server to)
	{
		Split s = this.getSplit();
		if (s == null)
			return;

		s.to = to.getName();
		s.end = new Date();

		try
		{
			PreparedStatement statement = Moo.db.prepare("UPDATE `splits` SET `to` = ?, `end` = ?, `reconnectedBy` = ? WHERE `name` = ? AND `from` = ? AND `when` = ?");
			statement.setString(1, s.to);
			statement.setDate(2, new java.sql.Date(s.end.getTime()));
			statement.setString(3, s.reconnectedBy);
			statement.setString(4, s.me);
			statement.setString(5, s.from);
			statement.setDate(6, new java.sql.Date(s.when.getTime()));
			Moo.db.executeUpdate(statement);
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
		}

		split = null;
	}

	public String getDesc()
	{
		return this.desc;
	}

	public void setDesc(final String d)
	{
		this.desc = d;
	}
}
