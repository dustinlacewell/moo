package net.rizon.moo.random;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.timer;

class deadListChecker extends timer
{
	public deadListChecker()
	{
		super(30, true);
	}

	@Override
	public void run(Date now)
	{
		for (Iterator<floodList> it = random.getFloodLists().iterator(); it.hasNext();)
		{
			floodList p = it.next();
			
			if (p.isClosed == false && p.getTimes().getLast() + random.timeforMatches < System.currentTimeMillis() / 1000L)
			{
				for (int c = 0; c < moo.conf.getFloodChannels().length; ++c)
					moo.privmsg(moo.conf.getFloodChannels()[c], "[FLOOD] End of flood for " + p.toString() + " - " + p.getMatches().size() + " matches");
				
				p.onClose();
				p.isClosed = true;
			}
		}
	}
}

public class random extends mpackage
{
	protected static final int maxSize = 100, matchesForFlood = 40, timeforMatches = 60, scoreForRandom = 3;
	
	public random()
	{
		super("Random", "Detects flood and random nicks");
		
		new commandFlood(this);
		new messageNotice();
		
		new deadListChecker().start();
	}
	
	protected static globalFloodList globalFlood = null;
	private static LinkedList<nickData> nicks = new LinkedList<nickData>();
	
	public static LinkedList<nickData> getNicks()
	{
		return nicks;
	}
	
	public static void addNickData(nickData nd)
	{	
		nicks.addLast(nd);
		nd.inc();
		
		if (nicks.size() > maxSize)
		{
			nd = nicks.removeFirst();
			nd.dec();
		}
		
		nd = nicks.getFirst();
		
		boolean flood = System.currentTimeMillis() / 1000L - nd.time <= timeforMatches;
		if (globalFlood == null && flood)
		{
			for (int c = 0; c < moo.conf.getFloodChannels().length; ++c)
				moo.privmsg(moo.conf.getFloodChannels()[c], "[FLOOD] Flood from incoming clients (" + nicks.size() + " in " + timeforMatches + " seconds), collecting sufficiently random users...");
			
			globalFlood = new globalFloodList();
			
			for (Iterator<nickData> it = nicks.iterator(); it.hasNext();)
			{
				nd = it.next();
				
				if (nd.getScore() >= scoreForRandom)
				{
					globalFlood.addMatch(nd);
					logMatch(nd, null, nd.toString());
				}
			}
			
			addFloodList(globalFlood);
		}
		else if (globalFlood != null && flood)
		{
			if (nd.getScore() >= scoreForRandom)
			{
				globalFlood.addMatch(nd);
				logMatch(nd, null, nd.toString());
			}
		}
	}
	
	private static LinkedList<floodList> floodLists = new LinkedList<floodList>();
	
	public static void addFloodList(floodList fl)
	{
		floodLists.add(fl);
	}
	
	public static LinkedList<floodList> getFloodLists()
	{
		return floodLists;
	}
	
	public static floodList getFloodListAt(int i)
	{
		try
		{
			return floodLists.get(i);
		}
		catch (IndexOutOfBoundsException ex)
		{
			return null;
		}
	}
	
	public static void removeFloodListAt(int i)
	{
		try
		{
			floodLists.remove(i);
		}
		catch (IndexOutOfBoundsException ex)
		{
		}
	}
	
	public static void logMatch(nickData nd, floodList fl, final String data)
	{
		for (int c = 0; c < moo.conf.getFloodChannels().length; ++c)
			moo.privmsg(moo.conf.getFloodChannels()[c], "[FLOOD MATCH " + fl.toString() + "] for " + data + " on " + nd.nick_str + " (" + nd.user_str + "@" + nd.ip + ") [" + nd.realname_str + "]");
	}
}
