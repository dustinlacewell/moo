package net.rizon.moo.random;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Timer;

class deadListChecker extends Timer
{
	public deadListChecker()
	{
		super(30, true);
	}

	@Override
	public void run(Date now)
	{
		long now_l = System.currentTimeMillis() / 1000L;
		
		for (Iterator<FloodList> it = FloodList.getActiveLists().iterator(); it.hasNext();)
		{
			FloodList p = it.next();
			
			if (p.isClosed)
				continue;
			
			if (p.getMatches().isEmpty() || now_l - p.getTimes().getFirst() > random.timeforMatches)
			{
				for (int c = 0; c < Moo.conf.getFloodChannels().length; ++c)
					Moo.privmsg(Moo.conf.getFloodChannels()[c], "[FLOOD] End of flood for " + p.toString() + " - " + p.getMatches().size() + " matches");
				
				/* Don't really close this, we want the list to persist forever. */
				p.isClosed = true;
			}
		}
	}
}

public class random extends Plugin
{
	protected static final int maxSize = 100, matchesForFlood = 20, timeforMatches = 60, scoreForRandom = 3, reconnectFloodLimit = 200;
	
	private Command flood;
	private Event e;
	private Timer dl;
	
	public random()
	{
		super("Random", "Detects flood and random nicks");
	}
	

	@Override
	public void start() throws Exception
	{
		flood = new CommandFlood(this);
		e = new EventRandom();
		dl = new deadListChecker();
		
		dl.start();
	}

	@Override
	public void stop()
	{
		flood.remove();
		e.remove();
		dl.stop();
	}
	
	private static LinkedList<NickData> nicks = new LinkedList<NickData>();
	
	public static LinkedList<NickData> getNicks()
	{
		return nicks;
	}
	
	public static void addNickData(NickData nd)
	{
		nicks.addLast(nd);
		nd.addToLists();
		
		if (nicks.size() > maxSize)
		{
			nd = nicks.removeFirst();
			nd.delFromLists();
		}
	}
	
	public static void logMatch(NickData nd, FloodList fl)
	{
		for (int c = 0; c < Moo.conf.getFloodChannels().length; ++c)
			Moo.privmsg(Moo.conf.getFloodChannels()[c], "[FLOOD MATCH " + fl + "] " + nd.nick_str + " (" + nd.user_str + "@" + nd.ip + ") [" + nd.realname_str + "]");
	}
	
	protected static void akill(final String ip)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT OR IGNORE INTO `akills` (`ip`, `count`) VALUES(?, 0)");
			stmt.setString(1, ip);
			Moo.db.executeUpdate();
			
			stmt = Moo.db.prepare("UPDATE AKILLS SET `count` = `count` + 1 WHERE `ip` = ?");
			stmt.setString(1, ip);
			Moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
		}
	}
	
	protected static boolean remove(final String ip)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("DELETE FROM `akills` WHERE `ip` = ?");
			stmt.setString(1, ip);
			return Moo.db.executeUpdate() == 1;
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
		}
		
		return false;
	}
}
