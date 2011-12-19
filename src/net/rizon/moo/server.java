package net.rizon.moo;

import java.util.Date;
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
	private LinkedList<split> splits = new LinkedList<split>();

	public long bytes = 0;

	public server(final String name)
	{
		this.name = name;
		servers.push(this);
		
		if (moo.conf.getDebug() > 0)
			System.out.println("Adding server " + this.getName());
		
		if (this.isHub())
			moo.sock.write("STATS c " + this.getName());
		if (this.isServices())
			moo.sock.write("STATS o " + this.getName());
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
		return this.getSID().endsWith("H");
	}
	
	public final boolean isServices()
	{
		return this.getSID().endsWith("S");
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
		if (this.splits.isEmpty() == false && this.splits.getLast().end == null)
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
		
		if (this.isHub())
		{
			moo.sock.write("STATS c " + this.getName());
			this.clines.clear();
		}
		
		if (this.isServices())
		{
			moo.sock.write("STATS o " + this.getName());
			this.olines.clear();
		}

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
	
	public static final LinkedList<server> getServers()
	{
		return servers;
	}
	
	public static void clearServers()
	{
		servers.clear();
	}
}