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
		for (Iterator<pattern> it = random.getFloodLists().iterator(); it.hasNext();)
		{
			pattern p = it.next();
			
			if (p.detached == false && p.last_adds.getLast() + random.timeforMatches < System.currentTimeMillis() / 1000L)
			{
				for (int c = 0; c < moo.conf.getFloodChannels().length; ++c)
					moo.privmsg(moo.conf.getFloodChannels()[c], "[FLOOD] End of flood for " + p.toString() + " - " + p.getMatches().size() + " matches");
				
				pattern.removePattern(p);
				p.detached = true;
			}
		}
	}
}

public class random extends mpackage
{
	protected static final int maxSize = 100, matchesForFlood = 50, timeforMatches = 60;
	
	public random()
	{
		super("Random", "Detects random nicks");
		
		new commandFlood(this);
		new eventRandom();
		new messageNotice();
		
		new deadListChecker().start();
	}
	
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
	}
	
	private static LinkedList<pattern> floodLists = new LinkedList<pattern>();
	
	public static void addFloodPattern(pattern p)
	{
		floodLists.add(p);
	}
	
	public static LinkedList<pattern> getFloodLists()
	{
		return floodLists;
	}
	
	public static pattern getFloodListAt(int i)
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
	
	public static void logMatch(nickData nd, pattern p, final String data)
	{
		for (int c = 0; c < moo.conf.getFloodChannels().length; ++c)
			moo.privmsg(moo.conf.getFloodChannels()[c], "[FLOOD MATCH " + p.toString() + "] for " + data + " on " + nd.nick_str + " (" + nd.user_str + "@" + nd.ip + ") [" + nd.realname_str + "]");
	}
}
