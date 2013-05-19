package net.rizon.moo.random;

import java.util.Iterator;
import java.util.LinkedList;

class dnsbl extends floodList
{
	private String name;
	
	private dnsbl(final String name)
	{
		this.name = name;
		lists.add(this);
	}

	@Override
	public String toString()
	{
		return this.name;
	}
	
	private static LinkedList<dnsbl> lists = new LinkedList<dnsbl>();
	
	public static dnsbl getList(final String name)
	{
		for (Iterator<dnsbl> it = lists.iterator(); it.hasNext();)
		{
			dnsbl d = it.next();
			if (d.name.equals(name))
			{
				if (d.isClosed)
				{
					it.remove();
					break;
				}
				else
				{
					return d;
				}
			}
		}
		
		dnsbl d = new dnsbl(name);
		d.open();
		return d;
	}
}