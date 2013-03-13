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
		long now_l = System.currentTimeMillis() / 1000L;
		
		for (Iterator<floodList> it = floodList.getActiveLists().iterator(); it.hasNext();)
		{
			floodList p = it.next();
			
			if (p.isClosed)
				continue;
			
			if (p.getMatches().isEmpty() || now_l - p.getTimes().getFirst() > random.timeforMatches)
			{
				for (int c = 0; c < moo.conf.getFloodChannels().length; ++c)
					moo.privmsg(moo.conf.getFloodChannels()[c], "[FLOOD] End of flood for " + p.toString() + " - " + p.getMatches().size() + " matches");
				
				/* Don't really close this, we want the list to persist forever. Call onClose to detach the pattern */
				p.isClosed = true;
				p.onClose();
			}
		}
	}
}

public class random extends mpackage
{
	protected static final int maxSize = 100, matchesForFlood = 20, timeforMatches = 60, scoreForRandom = 3;
	
	public random()
	{
		super("Random", "Detects flood and random nicks");
		
		new commandFlood(this);
		
		new eventRandom();
		
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
		nd.addToLists();
		
		if (nicks.size() > maxSize)
		{
			nd = nicks.removeFirst();
			nd.delFromLists();
		}
	}
	
	public static void logMatch(nickData nd, floodList fl)
	{
		for (int c = 0; c < moo.conf.getFloodChannels().length; ++c)
			moo.privmsg(moo.conf.getFloodChannels()[c], "[FLOOD MATCH " + fl + "] " + nd.nick_str + " (" + nd.user_str + "@" + nd.ip + ") [" + nd.realname_str + "]");
	}
}
