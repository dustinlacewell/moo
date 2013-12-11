package net.rizon.moo.random;

import java.util.Iterator;
import java.util.LinkedList;

class DNSBL extends FloodList
{
	private String name;
	
	private DNSBL(final String name)
	{
		this.name = name;
		lists.add(this);
	}

	@Override
	public String toString()
	{
		return this.name;
	}
	
	private static LinkedList<DNSBL> lists = new LinkedList<DNSBL>();
	
	public static DNSBL getList(final String name)
	{
		for (Iterator<DNSBL> it = lists.iterator(); it.hasNext();)
		{
			DNSBL d = it.next();
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
		
		DNSBL d = new DNSBL(name);
		d.open();
		return d;
	}
}