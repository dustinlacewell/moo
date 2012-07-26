package net.rizon.moo.random;

import java.util.HashSet;
import java.util.LinkedList;

abstract class floodList
{
	private HashSet<nickData> matches = new HashSet<nickData>();
	private LinkedList<Long> times = new LinkedList<Long>();
	protected boolean isClosed = false;
	protected boolean isList = false;
	
	@Override
	public abstract String toString();
	
	public void addMatch(nickData nd)
	{
		this.addTime();
		matches.add(nd);
		nd.lists.add(this);
	}
	
	public void delMatch(nickData nd)
	{
		matches.remove(nd);
		nd.lists.remove(this);
	}
	
	public HashSet<nickData> getMatches()
	{
		return this.matches;
	}
	
	private void addTime()
	{
		this.times.addLast(System.currentTimeMillis() / 1000L);
		if (this.times.size() > random.matchesForFlood)
			this.times.removeFirst();
	}
	
	public final LinkedList<Long> getTimes()
	{
		return this.times;
	}
	
	public abstract void onClose();
}
