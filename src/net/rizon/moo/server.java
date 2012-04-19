package net.rizon.moo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class server
{
	public static long lastSplit = 0;
	public static int last_total_users = 0, cur_total_users = 0, work_total_users = 0;
	
	private String name;
	private Date created;
	private String sid = null;
	public HashSet<String> clines = new HashSet<String>(), clines_work = new HashSet<String>();
	public HashSet<String> olines = new HashSet<String>();
	public HashSet<String> links = new HashSet<String>();
	public HashMap<String, Long> dnsbl = new HashMap<String, Long>();
	private LinkedList<split> splits = new LinkedList<split>();
	public long bytes = 0;
	public int users = 0, last_users = 0;
	public LinkedList<String> preferred_links = new LinkedList<String>();
	public boolean frozen = false;

	public server(final String name)
	{
		this.name = name;
		servers.push(this);
		
		if (moo.conf.getDebug() > 0)
			System.out.println("Adding server " + this.getName());
		
		moo.sock.write("STATS c " + this.getName());
		moo.sock.write("STATS o " + this.getName());
		moo.sock.write("STATS B " + this.getName());
	}
	
	public void destroy()
	{
		if (moo.conf.getDebug() > 0)
			System.out.println("Removing server " + this.getName());
		
		try
		{
			PreparedStatement statement = moo.db.prepare("DELETE FROM servers WHERE `name` = ?");
			statement.setString(1, this.getName());
			moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			System.err.print("Error removing server from database");
			ex.printStackTrace();
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
		return (this.getSID() != null && this.getSID().endsWith("H")) || this.getName().endsWith(".hub");
	}
	
	public final boolean isServices()
	{
		if (this.getSID() != null && this.getSID().endsWith("S"))
			return true;
		else if (this.getSID() != null && this.getSID().endsWith("PY"))
			return true;
		else if (this.getName().equals("acid.rizon.net")) // ???
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
		
		split s = new split();
		s.me = this.getName();
		s.from = from;
		s.when = now;
		this.splits.addLast(s);
		if (this.splits.size() > 10)
			this.splits.removeFirst();
		
		lastSplit = System.currentTimeMillis() / 1000L;
	}
	
	public split getSplit()
	{
		if (this.links.isEmpty() && this.splits.isEmpty() == false && this.splits.getLast().end == null)
			return this.splits.getLast();
		return null;
	}
	
	public split[] getSplits()
	{
		split[] splits = new split[this.splits.size()];
		this.splits.toArray(splits);
		return splits;
	}
	
	public void splitDel(final String to)
	{
		if (this.getSplit() == null)
			return;
		
		moo.sock.write("STATS c " + this.getName());
		this.clines.clear();

		moo.sock.write("STATS o " + this.getName());
		this.olines.clear();
		
		moo.sock.write("STATS B " + this.getName());

		split s = this.getSplit();
		s.to = to;
		s.end = new Date();
	}
	
	private static LinkedList<server> servers = new LinkedList<server>();
	public static Date last_link = null, last_split = null;
	
	public static server findServer(final String name)
	{
		for (Iterator<server> it = servers.iterator(); it.hasNext();)
		{
			server s = it.next();
			if (moo.match(s.getName(), "*" + name + "*"))
				return s;
		}
		return null;
	}
	
	public static server findServerAbsolute(final String name)
	{
		for (Iterator<server> it = servers.iterator(); it.hasNext();)
		{
			server s = it.next();
			if (s.getName().equalsIgnoreCase(name))
				return s;
		}
		return null;
	}
	
	public static final server[] getServers()
	{
		server[] s = new server[servers.size()];
		servers.toArray(s);
		return s;
	}
	
	public static void clearServers()
	{
		servers.clear();
	}
	
	public static class db extends event
	{
		static
		{
			new db();
		}
		
		@Override
		protected void initDatabases() 
		{
			moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS splits (`name` varchar(64), `from` varchar(64), `to` varchar(64), `when` date, `end` date);");
			moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS servers (`name`, `created` DATE DEFAULT CURRENT_TIMESTAMP, `preferred_links`, `frozen`);");
			moo.db.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS `name_idx` on `servers` (`name`);");
		}

		@Override
		public void loadDatabases()
		{
			try
			{
				for (Iterator<server> it = servers.iterator(); it.hasNext();)
					it.next().splits.clear();

				int count = 0;
				ResultSet rs = moo.db.executeQuery("SELECT * FROM splits");
				while (rs.next())
				{
					String name = rs.getString("name"), from = rs.getString("from"), to = rs.getString("to");
					Date when = rs.getDate("when"), end = rs.getDate("end");
					
					server s = server.findServerAbsolute(name);
					if (s == null)
						s = new server(name);
					split sp = new split();
					sp.me = name;
					sp.from = from;
					sp.to = to;
					sp.when = when;
					sp.end = end;
					s.splits.add(sp);
					
					++count;
				}
				
				System.out.println("Loaded " + count + " splits");
			}
			catch (SQLException ex)
			{
				database.handleException(ex);
			}
			
			try
			{
				for (Iterator<server> it = servers.iterator(); it.hasNext();)
					it.next().preferred_links.clear();
				
				ResultSet rs = moo.db.executeQuery("SELECT * FROM servers");
				while (rs.next())
				{
					String name = rs.getString("name"), pl = rs.getString("preferred_links");
					Date created = rs.getDate("created");
					boolean frozen = rs.getBoolean("frozen");
					
					server s = server.findServerAbsolute(name);
					if (s == null)
						s = new server(name);
					s.created = created;
					for (String l : pl.split(" "))
						if (l.trim().isEmpty() == false)
							s.preferred_links.add(l.trim());
					s.frozen = frozen;
				}
			}
			catch (SQLException ex)
			{
				database.handleException(ex);
			}
		}

		@Override
		public void saveDatabases()
		{
			try
			{
				moo.db.executeUpdate("DELETE FROM splits");
				
				PreparedStatement statement = moo.db.prepare("INSERT INTO splits (`name`, `from`, `to`, `when`, `end`) VALUES(?, ?, ?, ?, ?)"); 
				
				for (server s : server.getServers())
				{
					split[] splits = s.getSplits();
					
					for (split sp : splits)
					{
						statement.setString(1, sp.me);
						statement.setString(2, sp.from);
						statement.setString(3, sp.to);
						statement.setDate(4, new java.sql.Date(sp.when.getTime()));
						statement.setDate(5, (sp.end != null ? new java.sql.Date(sp.end.getTime()) : null));
						moo.db.executeUpdate();
					}
				}
			}
			catch (SQLException ex)
			{
				System.out.println("Error saving splits");
				ex.printStackTrace();
			}
			
			try
			{
				PreparedStatement statement = moo.db.prepare("REPLACE INTO servers (`name`, `preferred_links`, `frozen`) VALUES(?, ?, ?)"); 
				
				for (server s : server.getServers())
				{
					statement.setString(1, s.getName());
					String links = "";
					for (Iterator<String> it = s.preferred_links.iterator(); it.hasNext();)
						links += it.next() + " ";
					links = links.trim();
					statement.setString(2, links);
					statement.setBoolean(3, s.frozen);
					
					moo.db.executeUpdate();
				}
				
			}
			catch (SQLException ex)
			{
				System.out.println("Error saving servers");
				ex.printStackTrace();
			}
		}
	}
}