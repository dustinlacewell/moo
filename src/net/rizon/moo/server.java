package net.rizon.moo;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class server
{
	private String name;
	private String sid;
	public HashSet<String> clines = new HashSet<String>();
	public HashSet<String> links = new HashSet<String>();

	public String split_from;
	public Date split_when;
	public long bytes; 
	
	public static Date last_link = null;

	public server(final String name)
	{
		this.name = name;
		this.sid = null;
		this.split_from = null;
		this.split_when = null;
		this.bytes = 0;
		servers.push(this);
		
		if (moo.conf.getDebug() > 0)
			System.out.println("Adding server " + this.getName());
		
		if (this.isHub())
			moo.sock.write("STATS c " + this.getName());
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
		return this.getName().endsWith(".hub");
	}
	
	public void link(final String to)
	{
		this.links.add(to);
		last_link = new Date();
	}
	
	public void split(final String from)
	{
		this.links.remove(from);
		
		this.split_from = from;
		this.split_when = new Date();
	}
	
	public final boolean isSplit()
	{
		return this.split_from != null;
	}
	
	public void splitDel()
	{		
		if (this.split_from != null && this.split_when != null && this.isHub())
		{
			moo.sock.write("STATS c " + this.getName());
			this.clines.clear();
		}

		this.split_from = null;
		this.split_when = null;
	}
	
	private static LinkedList<server> servers = new LinkedList<server>();
	
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