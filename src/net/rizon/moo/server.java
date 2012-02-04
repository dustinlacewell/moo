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
	private String name;
	private String sid = null;
	public HashSet<String> clines = new HashSet<String>();
	public HashSet<String> olines = new HashSet<String>();
	public HashSet<String> links = new HashSet<String>();
	public HashMap<String, Long> dnsbl = new HashMap<String, Long>();
	private LinkedList<split> splits = new LinkedList<split>();

	public long bytes = 0;

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
		servers.remove(this);
	}
	
	public final String getName()
	{
		return this.name;
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
		this.dnsbl.clear();

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
	
	static class db extends table
	{
		@Override
		protected void init() 
		{
			moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS splits (`name` varchar(64), `from` varchar(64), `to` varchar(64), `when` date, `end` date);");
		}

		@Override
		public void load()
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
		}

		@Override
		public void save()
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
		}
	}
	
	static
	{
		new db();
	}
}