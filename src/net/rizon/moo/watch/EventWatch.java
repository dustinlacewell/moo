package net.rizon.moo.watch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;

import net.rizon.moo.Event;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.random.NickData;
import net.rizon.moo.random.random;

class EventWatch extends Event
{
	private static final Logger log = Logger.getLogger(EventWatch.class.getName());
	
	@Override
	protected void initDatabases()
	{
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `watches` (`nick` varchar(64), `creator` varchar(64), `reason` varchar(64), `created` date, `expires` date, `registered` varchar(64));");
	}
	
	@Override
	public void loadDatabases()
	{
		try
		{
			ResultSet rs = Moo.db.executeQuery("SELECT * FROM `watches`");
			while (rs.next())
			{
				WatchEntry we = new WatchEntry();
				
				we.nick = rs.getString("nick");
				we.creator = rs.getString("creator");
				we.reason = rs.getString("reason");
				we.created = new Date(rs.getDate("created").getTime());
				we.expires = new Date(rs.getDate("expires").getTime());
				we.registered = WatchEntry.registeredState.valueOf(rs.getString("registered"));
				
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
			Moo.db.executeUpdate("DELETE FROM `watches`");
			
			PreparedStatement statement = Moo.db.prepare("INSERT INTO `watches` (`nick`, `creator`, `reason`, `created`, `expires`, `registered`) VALUES(?, ?, ?, ?, ?, ?);");
			
			for (Iterator<WatchEntry> it = watch.watches.iterator(); it.hasNext();)
			{
				WatchEntry e = it.next();
				
				statement.setString(1, e.nick);
				statement.setString(2, e.creator);
				statement.setString(3, e.reason);
				statement.setDate(4, new java.sql.Date(e.created.getTime()));
				statement.setDate(5, new java.sql.Date(e.expires.getTime()));
				statement.setString(6, e.registered.toString());
				
				Moo.db.executeUpdate();
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
			for (Iterator<NickData> it = random.getNicks().iterator(); it.hasNext();)
			{
				NickData nd = it.next();
				
				if (ip.equals(nd.ip))
				{	
					for (Iterator<WatchEntry> it2 = watch.watches.iterator(); it2.hasNext();)
					{
						WatchEntry e = it2.next();
						
						if (e.nick.equalsIgnoreCase(nd.nick_str))
							return;
					}

					WatchEntry we = new WatchEntry();
					we.nick = nd.nick_str;
					we.creator = Moo.conf.getString("nick");
					// Do not move IP from the end of the reason
					we.reason = "Suspected open proxy (" + reason + ") on " + ip;
					we.created = new Date();
					we.expires = new Date(System.currentTimeMillis() + ban_time);
					we.registered = WatchEntry.registeredState.RS_UNKNOWN;
						
					watch.watches.add(we);
					
					for (String s : Moo.conf.getList("spam_channels"))
						Moo.privmsg(s, "Added watch for " + nd.nick_str + " due to hitting the OPM.");
					
					return;
				}
			}
		}
	}
	
	@Override
	public void onAkillDel(final String setter, final String ip, final String reason)
	{
		for (Iterator<WatchEntry> it = watch.watches.iterator(); it.hasNext();)
		{
			WatchEntry e = it.next();
			
			// NOTE: This relies on the fact that the IP comes at the end of the reason
			if (!e.reason.startsWith("Suspected open proxy") || !e.reason.endsWith(ip))
				continue;
			
			for (String s : Moo.conf.getList("spam_channels"))
				Moo.privmsg(s, "Removed watch for " + e.nick + " due to removal of respective akill for " + ip + ".");
			
			it.remove();
		}
	}
}
