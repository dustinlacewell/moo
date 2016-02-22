package net.rizon.moo.plugin.watch;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Iterator;

import net.rizon.moo.Moo;
import net.rizon.moo.conf.Config;
import net.rizon.moo.events.EventAkillDel;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.events.EventOPMHit;
import net.rizon.moo.events.InitDatabases;
import net.rizon.moo.events.LoadDatabases;
import net.rizon.moo.irc.Protocol;
import org.slf4j.Logger;

class EventWatch implements EventListener
{
	@Inject
	private static Logger logger;
	
	@Inject
	private Protocol protocol;
	
	@Inject
	private Config conf;
	
	@Inject
	private watch watch;

	@Subscribe
	public void initDatabases(InitDatabases evt)
	{
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `watches` (`nick` varchar(64) UNIQUE, `creator` varchar(64), `reason` varchar(64), `created` date, `expires` date, `registered` varchar(64));");
	}

	@Subscribe
	public void loadDatabases(LoadDatabases evt)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("SELECT * FROM `watches`");
			ResultSet rs = Moo.db.executeQuery(stmt);
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
			rs.close();
			stmt.close();
		}
		catch (Exception ex)
		{
			logger.warn("Unable to load watch database", ex);
		}
	}

	private static final long ban_time = 86400 * 3 * 1000L; // 3d

	@Subscribe
	public void onOPMHit(EventOPMHit evt)
	{
		String nick = evt.getNick(), ip = evt.getIp(), reason = evt.getReason();
		
		for (Iterator<WatchEntry> it = watch.watches.iterator(); it.hasNext();)
		{
			WatchEntry e = it.next();

			if (e.nick.equalsIgnoreCase(nick))
				return;
		}

		WatchEntry we = new WatchEntry();
		we.nick = nick;
		we.creator = conf.general.nick;
		// Do not move IP from the end of the reason
		we.reason = "Suspected open proxy (" + reason + ") on " + ip;
		we.created = new Date();
		we.expires = new Date(System.currentTimeMillis() + ban_time);
		we.registered = WatchEntry.registeredState.RS_UNKNOWN;

		watch.watches.add(we);
		
		watch.insert(we);

		protocol.privmsgAll(conf.spam_channels, "Added watch for " + nick + " due to hitting the OPM.");
	}

	@Subscribe
	public void onAkillDel(EventAkillDel evt)
	{
		String ip = evt.getIp();
		
		for (Iterator<WatchEntry> it = watch.watches.iterator(); it.hasNext();)
		{
			WatchEntry e = it.next();

			// NOTE: This relies on the fact that the IP comes at the end of the reason
			if (!e.reason.startsWith("Suspected open proxy") || !e.reason.endsWith(ip))
				continue;

			protocol.privmsgAll(conf.spam_channels, "Removed watch for " + e.nick + " due to removal of respective akill for " + ip + ".");

			watch.remove(e);
			it.remove();
		}
	}
}
