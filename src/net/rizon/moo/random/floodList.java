package net.rizon.moo.random;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import net.rizon.moo.moo;

abstract class floodList
{
	private HashSet<nickData> matches = new HashSet<nickData>();
	private LinkedList<Long> times = new LinkedList<Long>();
	protected boolean isList = false;
	protected boolean isClosed = false;
	
	@Override
	public abstract String toString();
	
	protected final void addMatch(nickData nd)
	{
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
			for (int c = 0; c < moo.conf.getFloodChannels().length; ++c)
				moo.privmsg(moo.conf.getFloodChannels()[c], "[FLOOD] Pattern " + this + " detected in incoming clients (" + random.matchesForFlood + " out of last " + random.maxSize + " users), collecting matching users...");
				
			this.isList = true;
				
			for (Iterator<nickData> it = this.getMatches().iterator(); it.hasNext();)
				random.logMatch(it.next(), this);
		}
	}
	
	protected final void delMatch(nickData nd)
	{
		if (this.isList)
			return;
		
		this.matches.remove(nd);
		
		if (this.matches.isEmpty())
			this.close();
	}
	
	public HashSet<nickData> getMatches()
	{
		return this.matches;
	}

	public final LinkedList<Long> getTimes()
	{
		return this.times;
	}
	
	abstract protected void onClose();
	
	protected void open()
	{
		lists.add(this);
	}
	
	protected void close()
	{
		this.isClosed = true;
		lists.remove(this);
		this.onClose();
	}
	
	private static LinkedList<floodList> lists = new LinkedList<floodList>();
	
	protected static LinkedList<floodList> getAllLists()
	{
		return lists;
	}
	
	protected static LinkedList<floodList> getActiveLists()
	{
		LinkedList<floodList> l = new LinkedList<floodList>();
		for (Iterator<floodList> it = lists.iterator(); it.hasNext();)
		{
			floodList fl = it.next();
			if (fl.isList)
				l.add(fl);
		}
		return l;
	}
	
	protected static void clearFloodLists()
	{
		for (Iterator<floodList> it = getActiveLists().iterator(); it.hasNext();)
			it.next().close();
	}
	
	protected static floodList getFloodListAt(int i)
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
