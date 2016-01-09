package net.rizon.moo.plugin.watch;

import io.netty.util.concurrent.ScheduledFuture;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.rizon.moo.Command;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class watch extends Plugin
{
	private static final Logger logger = LoggerFactory.getLogger(watch.class);
	public static List<WatchEntry> watches = new ArrayList<>();

	private Message watchReply;
	private EventWatch e;
	private Message n;
	private Command w;
	private ScheduledFuture watchMonitor;

	public watch()
	{
		super("Watch", "Disallows nicks to be used");
	}

	@Override
	public void start() throws Exception
	{
		e = new EventWatch();
		Moo.getEventBus().register(e);
		
		n = new MessageNotice();
		w = new CommandWatch(this);
		watchMonitor = Moo.scheduleWithFixedDelay(new WatchMonitor(), 1, TimeUnit.MINUTES);
		watchReply = new WatchReply();
	}

	@Override
	public void stop()
	{
		e = new EventWatch();
		Moo.getEventBus().unregister(e);
		
		n.remove();
		w.remove();
		watchMonitor.cancel(false);
		watchReply.remove();
	}
	
	public static void insert(WatchEntry e)
	{
		try
		{
			PreparedStatement statement = Moo.db.prepare("INSERT OR REPLACE INTO `watches` (`nick`, `creator`, `reason`, `created`, `expires`, `registered`) VALUES(?, ?, ?, ?, ?, ?);");

			statement.setString(1, e.nick);
			statement.setString(2, e.creator);
			statement.setString(3, e.reason);
			statement.setDate(4, new java.sql.Date(e.created.getTime()));
			statement.setDate(5, new java.sql.Date(e.expires.getTime()));
			statement.setString(6, e.registered.toString());

			Moo.db.executeUpdate(statement);
		}
		catch (SQLException ex)
		{
			logger.warn("unable to insert watch " + e, ex);
		}
	}
	
	public static void remove(WatchEntry e)
	{
		try
		{
			PreparedStatement statement = Moo.db.prepare("DELETE FROM `watches` WHERE `nick` = ?");
		
			statement.setString(1, e.nick);
			
			Moo.db.executeUpdate(statement);
		}
		catch (SQLException ex)
		{
			logger.warn("unable to remove watch " + e, ex);
		}
	}
}
