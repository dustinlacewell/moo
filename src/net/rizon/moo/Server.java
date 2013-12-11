package net.rizon.moo;

import java.security.cert.X509Certificate;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
	public HashMap<String, String> olines = new HashMap<String, String>(), olines_work = new HashMap<String, String>();
	public HashSet<String> links = new HashSet<String>();
	public HashMap<String, Long> dnsbl = new HashMap<String, Long>();
	public long bytes = 0;
	public int users = 0, last_users = 0;
	public LinkedList<String> preferred_links = new LinkedList<String>();
	public boolean frozen = false;
	/* from /stats u */
	public Date uptime;
	/* public cert for the ircd */
	public X509Certificate cert;

	public Server(final String name)
	{
		this.name = name;
		servers.push(this);
		
		log.log(Level.FINE, "Adding server " + this.getName());
		
		Moo.sock.write("STATS c " + this.getName());
		Moo.sock.write("STATS o " + this.getName());
		Moo.sock.write("STATS B " + this.getName());
		if (!this.isServices())
			Moo.sock.write("VERSION " + this.getName());
		
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
			Moo.db.executeUpdate();
			
			statement = Moo.db.prepare("DELETE FROM splits WHERE `name` = ?");
			statement.setString(1, this.getName());
			Moo.db.executeUpdate();
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
		if (this.getSID() != null && this.getSID().endsWith("S"))
			return true;
		else if (this.getSID() != null && this.getSID().endsWith("PY"))
			return true;
		else if (this.getName().endsWith(".rizon.net"))
			return true;
		else if (this.getName().startsWith("services."))
			return true;
		
		return false;
	}
	
	public void link(final String to)
	{
		this.links.add(to);
		last_link = new Date();
	}
	
	public void split(final String from)
	{
		Date now = new Date();
		this.links.remove(from);
		last_split = now;
		
		Split s = new Split();
		s.me = this.getName();
		s.from = from;
		s.when = now;
		
		try
		{
			PreparedStatement statement = Moo.db.prepare("INSERT INTO splits (`name`, `from`, `to`, `when`, `end`, `reconnectedBy`) VALUES(?, ?, ?, ?, ?, ?)");
			statement.setString(1, s.me);
			statement.setString(2, s.from);
			statement.setString(3, s.to);
			statement.setDate(4, new java.sql.Date(s.when.getTime()));
			statement.setDate(5, (s.end != null ? new java.sql.Date(s.end.getTime()) : null));
			statement.setString(6, s.reconnectedBy);
			Moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
		}
		
		lastSplit = System.currentTimeMillis() / 1000L;
	}
	
	public Split getSplit()
	{
		if (this.links.isEmpty())
		{
			try
			{
				PreparedStatement statement = Moo.db.prepare("SELECT * FROM `splits` WHERE `name` = ? ORDER BY `when` DESC LIMIT 1");
				statement.setString(1, this.getName());
				ResultSet rs = Moo.db.executeQuery();
				if (rs.next())
				{
					String name = rs.getString("name"), from = rs.getString("from"), to = rs.getString("to"), rBy = rs.getString("reconnectedBy");
					Date when = rs.getDate("when"), end = rs.getDate("end");
	
					Split sp = new Split();
					sp.me = name;
					sp.from = from;
					sp.to = to;
					sp.when = when;
					sp.end = end;
					sp.reconnectedBy = rBy;
					
					if (sp.end == null)
						return sp;
				}
			}
			catch (SQLException ex)
			{
				Database.handleException(ex);
			}
		}
		return null;
	}
	
	public Split[] getSplits()
	{
		try
		{
			LinkedList<Split> splits = new LinkedList<Split>();
			PreparedStatement stmt = Moo.db.prepare("SELECT * FROM `splits` WHERE `name` = ?");
			stmt.setString(1, this.getName());
			ResultSet rs = Moo.db.executeQuery();
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
	
	public void splitDel(final String to)
	{
		Split s = this.getSplit();
		if (s == null)
			return;
		
		Moo.sock.write("STATS c " + this.getName());
		Moo.sock.write("STATS o " + this.getName());
		Moo.sock.write("STATS B " + this.getName());

		s.to = to;
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
			Moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
		}
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
		for (Iterator<Server> it = servers.iterator(); it.hasNext();)
		{
			Server s = it.next();
			if (Moo.matches(s.getName(), "*" + name + "*"))
				return s;
		}
		return null;
	}
	
	public static Server findServerAbsolute(final String name)
	{
		for (Iterator<Server> it = servers.iterator(); it.hasNext();)
		{
			Server s = it.next();
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
	
	public static void clearServers()
	{
		servers.clear();
	}
	
	public static class db extends Event
	{
		static
		{
			new db();
		}
		
		@Override
		protected void initDatabases() 
		{
			Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS splits (`name` varchar(64), `from` varchar(64), `to` varchar(64), `when` date, `end` date, `reconnectedBy` varchar(64));");
			Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS servers (`name`, `created` DATE DEFAULT CURRENT_TIMESTAMP, `desc`, `preferred_links`, `frozen`);");
			Moo.db.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS `servers_name_idx` on `servers` (`name`);");
		}

		@Override
		public void loadDatabases()
		{
			try
			{
				for (Iterator<Server> it = servers.iterator(); it.hasNext();)
					it.next().preferred_links.clear();
				
				ResultSet rs = Moo.db.executeQuery("SELECT * FROM servers");
				while (rs.next())
				{
					String name = rs.getString("name"), desc = rs.getString("desc"), pl = rs.getString("preferred_links");
					Date created = rs.getDate("created");
					boolean frozen = rs.getBoolean("frozen");
					
					Server s = Server.findServerAbsolute(name);
					if (s == null)
						s = new Server(name);
					if (desc != null)
						s.desc = desc;
					s.created = created;
					for (String l : pl.split(" "))
						if (l.trim().isEmpty() == false)
							s.preferred_links.add(l.trim());
					s.frozen = frozen;
				}
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
				PreparedStatement statement = Moo.db.prepare("REPLACE INTO servers (`name`, `desc`, `preferred_links`, `frozen`) VALUES(?, ?, ?, ?)"); 
				
				for (Server s : Server.getServers())
				{
					statement.setString(1, s.getName());
					statement.setString(2, s.desc);
					String links = "";
					for (Iterator<String> it = s.preferred_links.iterator(); it.hasNext();)
						links += it.next() + " ";
					links = links.trim();
					statement.setString(3, links);
					statement.setBoolean(4, s.frozen);
					
					Moo.db.executeUpdate();
				}
				
			}
			catch (SQLException ex)
			{
				log.log(Level.WARNING, "Error saving servers", ex);
			}
		}
	}
}