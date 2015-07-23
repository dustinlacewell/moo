package net.rizon.moo;

import java.security.cert.X509Certificate;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;

public class Server
{
	private static final Logger log = Logger.getLogger(Server.class.getName());
	public static long lastSplit = 0;
	public static int last_total_users = 0, cur_total_users = 0, work_total_users = 0;

	private String name;
	private Date created;
	private String desc = "";
	private String sid = null;
	public HashSet<String> clines = new HashSet<String>(), clines_work = new HashSet<String>();
	// oper name -> flags
	public HashMap<String, String> olines, olines_work = new HashMap<String, String>();
	public Server uplink;
	public static Server root;
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
		servers.push(this);

		log.log(Level.FINE, "Adding server " + this.getName());

		if (Moo.sock != null)
		{
			this.requestStats();
			if (!this.isServices())
				Moo.sock.write("VERSION " + this.getName());
			Moo.sock.write("MAP");
		}

		for (Event e : Event.getEvents())
			e.onServerCreate(this);
	}

	public void destroy()
	{
		log.log(Level.FINE, "Removing server " + this.getName());

		for (Event e : Event.getEvents())
			e.onServerDestroy(this);

		try
		{
			PreparedStatement statement = Moo.db.prepare("DELETE FROM servers WHERE `name` = ?");
			statement.setString(1, this.getName());
			Moo.db.executeUpdate(statement);

			statement = Moo.db.prepare("DELETE FROM splits WHERE `name` = ?");
			statement.setString(1, this.getName());
			Moo.db.executeUpdate(statement);
		}
		catch (SQLException ex)
		{
			log.log(Level.SEVERE, "Error removing server from database", ex);
		}

		servers.remove(this);
	}

	public final String getName()
	{
		return this.name;
	}

	public final Date getCreated()
	{
		return this.created;
	}

	public void setSID(final String s)
	{
		this.sid = s;
	}

	public final String getSID()
	{
		return this.sid;
	}

	public final boolean isHub()
	{
		return (this.getSID() != null && this.getSID().endsWith("H")) || this.getName().endsWith(".hub") || this.getName().startsWith("hub.");
	}

	public final boolean isServices()
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
		last_link = new Date();
	}

	public void split(Server from)
	{
		Date now = new Date();
		this.links.remove(from);
		last_split = now;

		Split s = new Split();
		s.me = this.getName();
		s.from = from.name;
		s.when = now;
		s.recursive = false;

		// Find servers that split from this one at the same time
		for (Server serv : Server.getServers())
		{
			Split sp = serv.getSplit();
			if (sp != null && sp.from.equals(this.name) && sp.when.getTime() / 1000L == now.getTime() / 1000L)
			{
				sp.recursive = true;

				try
				{
					PreparedStatement statement = Moo.db.prepare("UPDATE splits SET `recursive` = ? WHERE `name` = ? AND `from` = ? AND `when` = ?");
					statement.setBoolean(1, sp.recursive);
					statement.setString(2, sp.me);
					statement.setString(3, sp.from);
					statement.setDate(4, new java.sql.Date(sp.when.getTime()));
					Moo.db.executeUpdate(statement);
				}
				catch (SQLException ex)
				{
					Database.handleException(ex);
				}
			}
		}

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

		lastSplit = System.currentTimeMillis() / 1000L;
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

		this.requestStats();

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

	private static LinkedList<Server> servers = new LinkedList<Server>();
	public static Date last_link = null, last_split = null;

	public static Server findServer(final String name)
	{
		for (Server s : servers)
		{
			if (Moo.matches(s.getName(), "*" + name + "*"))
				return s;
		}
		return null;
	}

	public static Server findServerAbsolute(final String name)
	{
		for (Server s : servers)
		{
			if (s.getName().equalsIgnoreCase(name))
				return s;
		}
		return null;
	}

	public static final Server[] getServers()
	{
		Server[] s = new Server[servers.size()];
		servers.toArray(s);
		return s;
	}

	public static class db extends Event
	{
		@Override
		protected void initDatabases()
		{
			Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS splits (`name` varchar(64), `from` varchar(64), `to` varchar(64), `when` date, `end` date, `reconnectedBy` varchar(64), `recursive`);");
			Moo.db.executeUpdate("CREATE INDEX IF NOT EXISTS `splits_idx` on `splits` (`name`,`when`,`from`)");
			Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS servers (`name`, `created` DATE DEFAULT CURRENT_TIMESTAMP, `desc`, `preferred_links`, `frozen`);");
			Moo.db.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS `servers_name_idx` on `servers` (`name`);");
		}

		@Override
		public void loadDatabases()
		{
			try
			{
				PreparedStatement stmt = Moo.db.prepare("SELECT * FROM servers");
				ResultSet rs = Moo.db.executeQuery(stmt);
				while (rs.next())
				{
					String name = rs.getString("name"), desc = rs.getString("desc"), pl = rs.getString("preferred_links");
					Date created = rs.getDate("created");
					boolean frozen = rs.getBoolean("frozen");

					Server s = Server.findServerAbsolute(name);
					if (s == null)
						s = new Server(name);
					else
						s.allowed_clines.clear();

					if (desc != null)
						s.desc = desc;
					s.created = created;
					for (String l : pl.split(" "))
						if (l.trim().isEmpty() == false)
							s.allowed_clines.add(l.trim());
					s.frozen = frozen;
				}
				rs.close();
				stmt.close();
			}
			catch (SQLException ex)
			{
				Database.handleException(ex);
			}
		}

		@Override
		public void saveDatabases()
		{
			try
			{
				PreparedStatement statement;

				for (Server s : Server.getServers())
				{
					statement = Moo.db.prepare("REPLACE INTO servers (`name`, `desc`, `preferred_links`, `frozen`) VALUES(?, ?, ?, ?)");
					statement.setString(1, s.getName());
					statement.setString(2, s.desc);
					String links = "";
					for (String string : s.allowed_clines)
						links += string + " ";
					links = links.trim();
					statement.setString(3, links);
					statement.setBoolean(4, s.frozen);

					Moo.db.executeUpdate(statement);
				}

			}
			catch (SQLException ex)
			{
				log.log(Level.WARNING, "Error saving servers", ex);
			}
		}
	}

	public void requestStats()
	{
		Moo.sock.write("STATS c " + this.getName());
		Moo.sock.write("STATS o " + this.getName());
	}

	public static void init()
	{
		new db();

		new Timer(300, true)
		{
			@Override
			public void run(Date now)
			{
				for (Server s : Server.getServers())
					if (!s.isServices())
						s.requestStats();
				Moo.sock.write("MAP");
			}
		}.start();
	}
}
