package net.rizon.moo.plugin.random;

import net.rizon.moo.Moo;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

abstract class FloodList
{
	private HashSet<NickData> matches = new HashSet<NickData>();
	private LinkedList<Long> times = new LinkedList<Long>();
	protected boolean isList = false;
	protected boolean isClosed = false;
	
	@Override
	public abstract String toString();
	
	protected final void addMatch(NickData nd)
	{
		for (NickData n : this.matches)
			if (n.isEqual(nd))
			{
				++n.hits;
				if (n.hits > random.reconnectFloodLimit)
				{
					Moo.privmsgAll(Moo.conf.flood_channels, "[FLOOD] Client " + n + " has hit flood list " + this + " multiple times (" + n.hits + ")");
					return;
				}
			}
		
		this.times.addLast(System.currentTimeMillis() / 1000L);
		if (this.times.size() > random.matchesForFlood)
			this.times.removeFirst();
		
		this.matches.add(nd);
		
		if (this.isList)
			random.logMatch(nd, this);
		
		long first = this.getTimes().getFirst();
		
		long now = System.currentTimeMillis() / 1000L;
		if (this.isList == false && now - first <= random.timeforMatches && this.getMatches().size() >= random.matchesForFlood)
		{
			Moo.privmsgAll(Moo.conf.flood_channels, "[FLOOD] Pattern " + this + " detected in incoming clients (" + random.matchesForFlood + " out of last " + random.maxSize + " users), collecting matching users...");
				
			this.isList = true;
				
			for (Iterator<NickData> it = this.getMatches().iterator(); it.hasNext();)
				random.logMatch(it.next(), this);
		}
	}
	
	protected final void delMatch(NickData nd)
	{
		if (this.isList)
			return;
		
		this.matches.remove(nd);
		
		if (this.matches.isEmpty())
			this.close();
	}
	
	public HashSet<NickData> getMatches()
	{
		return this.matches;
	}

	public final LinkedList<Long> getTimes()
	{
		return this.times;
	}
	
	/* the following assumes two flood lists are never .equal() unless == */
	
	protected void open()
	{
		lists.add(this);
	}
	
	protected void close()
	{
		this.isClosed = true;
		this.isList = false;
		this.matches.clear();
		lists.remove(this);
	}
	
	private static LinkedList<FloodList> lists = new LinkedList<FloodList>();
	
	protected static LinkedList<FloodList> getLists()
	{
		return lists;
	}
	
	protected static LinkedList<FloodList> getActiveLists()
	{
		LinkedList<FloodList> l = new LinkedList<FloodList>();
		for (Iterator<FloodList> it = lists.iterator(); it.hasNext();)
		{
			FloodList fl = it.next();
			if (fl.isList)
				l.add(fl);
		}
		return l;
	}
	
	protected static void clearFloodLists()
	{
		for (Iterator<FloodList> it = getActiveLists().iterator(); it.hasNext();)
			it.next().close();
	}
	
	protected static FloodList getFloodListAt(int i)
	{
		try
		{
			return getActiveLists().get(i);
		}
		catch (IndexOutOfBoundsException ex)
		{
			return null;
		}
	}
	
	protected static void removeFloodListAt(int i)
	{
		try
		{
			getActiveLists().get(i).close();
		}
		catch (IndexOutOfBoundsException ex)
		{
		}
	}
}
