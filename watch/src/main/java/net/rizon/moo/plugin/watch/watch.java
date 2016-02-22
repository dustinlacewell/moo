package net.rizon.moo.plugin.watch;

import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import io.netty.util.concurrent.ScheduledFuture;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.rizon.moo.Command;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.conf.Config;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.util.TimeDifference;
import org.slf4j.Logger;

public class watch extends Plugin
{
	@Inject
	private static Logger logger;
	
	@Inject
	private CommandWatch watch;
	
	@Inject
	private WatchMonitor watchMonitor;
	
	@Inject
	private Protocol protocol;
	
	@Inject
	private Config conf;
	
	public List<WatchEntry> watches = new ArrayList<>();

	private ScheduledFuture watchMonitorFuture;

	public watch()
	{
		super("Watch", "Disallows nicks to be used");
	}

	@Override
	public void start() throws Exception
	{
		watchMonitorFuture = Moo.scheduleWithFixedDelay(watchMonitor, 1, TimeUnit.MINUTES);
	}

	@Override
	public void stop()
	{
		watchMonitorFuture.cancel(false);
	}
	
	void insert(WatchEntry e)
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
	
	void remove(WatchEntry e)
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
	
	WatchEntry find(String nick)
	{
		for (WatchEntry e : watches)
			if (e.nick.equals(nick))
				return e;
		return null;
	}
	
	void handleWatch(WatchEntry w)
	{
		w.handled = true;

		if (w.registered == WatchEntry.registeredState.RS_UNKNOWN && w.requested_registered == false)
		{
			protocol.privmsg("NickServ", "INFO " + w.nick);
			w.requested_registered = true;
		}
		else if (w.registered == WatchEntry.registeredState.RS_NOT_REGISTERED || w.registered == WatchEntry.registeredState.RS_MANUAL_AKILL)
		{
			protocol.privmsgAll(conf.spam_channels, "WATCH: Akilling " + w.nick + " for: " + w.reason);
			protocol.qakill(w.nick, w.reason);
		}
		else if (w.registered == WatchEntry.registeredState.RS_MANUAL_CAPTURE && w.warned == false)
		{
			protocol.privmsgAll(conf.spam_channels, "WATCH: Capturing " + w.nick + " for: " + w.reason);
			protocol.capture(w.nick);
			w.warned = true;
		}
		else if (w.registered == WatchEntry.registeredState.RS_REGISTERED && w.warned == false)
		{
			for (String chan : conf.spam_channels)
				protocol.privmsg(chan, "PROXY: " + w.nick + " was detected on an open proxy on " + w.created + ", which was " + TimeDifference.difference(new Date(), w.created) + " ago.");
			w.warned = true;
		}
	}

	void handleOffline(WatchEntry w)
	{
		w.warned = false;
	}

	@Override
	public List<Command> getCommands()
	{
		return Arrays.<Command>asList(watch);
	}

	@Override
	protected void configure()
	{
		bind(watch.class).toInstance(this);
		
		bind(WatchMonitor.class);
		
		Multibinder<EventListener> eventListenerBinder = Multibinder.newSetBinder(binder(), EventListener.class);
		eventListenerBinder.addBinding().to(EventWatch.class);

		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder(), Command.class);
		commandBinder.addBinding().to(CommandWatch.class);
		
		Multibinder<Message> messageBinder = Multibinder.newSetBinder(binder(), Message.class);
		messageBinder.addBinding().to(MessageNotice.class);
		messageBinder.addBinding().to(WatchReply.class);
	}
}
