package net.rizon.moo.watch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;

import net.rizon.moo.event;
import net.rizon.moo.logger;
import net.rizon.moo.moo;
import net.rizon.moo.random.nickData;
import net.rizon.moo.random.random;

class eventWatch extends event
{
	private static final logger log = logger.getLogger(eventWatch.class.getName());
	
	@Override
	protected void initDatabases()
	{
		moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `watches` (`nick` varchar(64), `creator` varchar(64), `reason` varchar(64), `created` date, `expires` date, `registered` varchar(64));");
	}
	
	@Override
	public void loadDatabases()
	{
		try
		{
			ResultSet rs = moo.db.executeQuery("SELECT * FROM `watches`");
			while (rs.next())
			{
				watchEntry we = new watchEntry();
				
				we.nick = rs.getString("nick");
				we.creator = rs.getString("creator");
				we.reason = rs.getString("reason");
				we.created = new Date(rs.getDate("created").getTime());
				we.expires = new Date(rs.getDate("expires").getTime());
				we.registered = watchEntry.registeredState.valueOf(rs.getString("registered"));
				
				watch.watches.add(we);
			}
		}
		catch (Exception ex)
		{
			log.log(ex);
		}
	}
	
	@Override
	public void saveDatabases()
	{
		try
		{
			moo.db.executeUpdate("DELETE FROM `watches`");
			
			PreparedStatement statement = moo.db.prepare("INSERT INTO `watches` (`nick`, `creator`, `reason`, `created`, `expires`, `registered`) VALUES(?, ?, ?, ?, ?, ?);");
			
			for (Iterator<watchEntry> it = watch.watches.iterator(); it.hasNext();)
			{
				watchEntry e = it.next();
				
				statement.setString(1, e.nick);
				statement.setString(2, e.creator);
				statement.setString(3, e.reason);
				statement.setDate(4, new java.sql.Date(e.created.getTime()));
				statement.setDate(5, new java.sql.Date(e.expires.getTime()));
				statement.setString(6, e.registered.toString());
				
				moo.db.executeUpdate();
			}
		}
		catch (SQLException ex)
		{
			log.log(ex);
		}
	}
	
	private static final String opmMatch = "Using or hosting open proxies is not permitted";
	private static final long ban_time = 86400 * 3 * 1000L; // 3d
	
	@Override
	public void onAkillAdd(final String setter, final String ip, final String reason)
	{
		if (reason.contains(opmMatch))
		{
			for (Iterator<nickData> it = random.getNicks().iterator(); it.hasNext();)
			{
				nickData nd = it.next();
				
				if (ip.equals(nd.ip))
				{	
					for (Iterator<watchEntry> it2 = watch.watches.iterator(); it2.hasNext();)
					{
						watchEntry e = it2.next();
						
						if (e.nick.equalsIgnoreCase(nd.nick_str))
							return;
					}

					watchEntry we = new watchEntry();
					we.nick = nd.nick_str;
					we.creator = moo.conf.getNick();
					// Do not move IP from the end of the reason
					we.reason = "Suspected open proxy (" + reason + ") on " + ip;
					we.created = new Date();
					we.expires = new Date(System.currentTimeMillis() + ban_time);
					we.registered = watchEntry.registeredState.RS_UNKNOWN;
						
					watch.watches.add(we);
					
					for (int i = 0; i < moo.conf.getSpamChannels().length; ++i)
						moo.privmsg(moo.conf.getSpamChannels()[i], "Added watch for " + nd.nick_str + " due to hitting the OPM.");
					
					return;
				}
			}
		}
	}
	
	@Override
	public void onAkillDel(final String setter, final String ip, final String reason)
	{
		for (Iterator<watchEntry> it = watch.watches.iterator(); it.hasNext();)
		{
			watchEntry e = it.next();
			
			// NOTE: This relies on the fact that the IP comes at the end of the reason
			if (!e.reason.startsWith("Suspected open proxy") || !e.reason.endsWith(ip))
				continue;
			
			for (int i = 0; i < moo.conf.getSpamChannels().length; ++i)
				moo.privmsg(moo.conf.getSpamChannels()[i], "Removed watch for " + e.nick + " due to removal of respective akill for " + ip + ".");
			
			it.remove();
		}
	}
}
