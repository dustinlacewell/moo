package net.rizon.moo.plugin.random;

import io.netty.util.concurrent.ScheduledFuture;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DeadListChecker implements Runnable
{
	@Override
	public void run()
	{
		long now_l = System.currentTimeMillis() / 1000L;

		for (Iterator<FloodList> it = FloodList.getLists().iterator(); it.hasNext();)
		{
			FloodList p = it.next();

			if (p.isClosed)
				continue;

			if (p.getMatches().isEmpty() || now_l - p.getTimes().getFirst() > random.timeforMatches)
			{
				if (p.isList)
				{
					Moo.privmsgAll(Moo.conf.flood_channels, "[FLOOD] End of flood for " + p.toString() + " - " + p.getMatches().size() + " matches");

					/* Don't really close this, we want the list to persist forever. */
					p.isClosed = true;
				}
				else
				{
					/* List hasn't been touched in awhile, delete it */
					it.remove();
					p.close();
				}
			}
		}
	}
}

public class random extends Plugin
{
	protected static final int maxSize = 100, matchesForFlood = 20, timeforMatches = 60, scoreForRandom = 3, reconnectFloodLimit = 200;
	
	private static final Logger logger = LoggerFactory.getLogger(random.class);

	private Command flood;
	private Event e;
	private ScheduledFuture dl;

	public random()
	{
		super("Random", "Detects flood and random nicks");
	}


	@Override
	public void start() throws Exception
	{
		flood = new CommandFlood(this);
		e = new EventRandom();
		
		dl = Moo.scheduleWithFixedDelay(new DeadListChecker(), 30, TimeUnit.SECONDS);
	}

	@Override
	public void stop()
	{
		flood.remove();
		e.remove();
		dl.cancel(false);
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
		Moo.privmsgAll(Moo.conf.flood_channels, "[FLOOD MATCH " + fl + "] " + nd.nick_str + " (" + nd.user_str + "@" + nd.ip + ") [" + nd.realname_str + "]");
	}

	protected static void akill(final String ip)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT OR IGNORE INTO `akills` (`ip`, `count`) VALUES(?, 0)");
			stmt.setString(1, ip);
			Moo.db.executeUpdate(stmt);

			stmt = Moo.db.prepare("UPDATE AKILLS SET `count` = `count` + 1 WHERE `ip` = ?");
			stmt.setString(1, ip);
			Moo.db.executeUpdate(stmt);
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to log akill", ip);
		}
	}

	protected static boolean remove(final String ip)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("DELETE FROM `akills` WHERE `ip` = ?");
			stmt.setString(1, ip);
			return Moo.db.executeUpdate(stmt) == 1;
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to remove akill", ip);
		}

		return false;
	}
}
