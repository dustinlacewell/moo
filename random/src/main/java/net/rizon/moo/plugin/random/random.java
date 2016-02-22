package net.rizon.moo.plugin.random;

import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import io.netty.util.concurrent.ScheduledFuture;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.conf.Config;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.irc.Protocol;
import org.slf4j.Logger;

public class random extends Plugin
{
	protected static final int maxSize = 100, matchesForFlood = 20, timeforMatches = 60, scoreForRandom = 3, reconnectFloodLimit = 200;

	@Inject
	private static Logger logger;

	@Inject
	private CommandFlood flood;

	@Inject
	private DeadListChecker checker;

	@Inject
	private Protocol protocol;

	@Inject
	private Config conf;

	private ScheduledFuture dl;

	public random()
	{
		super("Random", "Detects flood and random nicks");
	}

	public Protocol getProtocol()
	{
		return protocol;
	}

	public Config getConf()
	{
		return conf;
	}

	@Override
	public void start() throws Exception
	{
		dl = Moo.scheduleWithFixedDelay(checker, 30, TimeUnit.SECONDS);
	}

	@Override
	public void stop()
	{
		dl.cancel(false);
	}

	private LinkedList<NickData> nicks = new LinkedList<>();

	public List<NickData> getNicks()
	{
		return nicks;
	}

	public void addNickData(NickData nd)
	{
		nicks.addLast(nd);
		nd.addToLists();

		if (nicks.size() > maxSize)
		{
			nd = nicks.removeFirst();
			nd.delFromLists();
		}
	}

	public void logMatch(NickData nd, FloodList fl)
	{
		protocol.privmsgAll(conf.flood_channels, "[FLOOD MATCH " + fl + "] " + nd.nick_str + " (" + nd.user_str + "@" + nd.ip + ") [" + nd.realname_str + "]");
	}

	protected void akill(final String ip)
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

	protected boolean remove(final String ip)
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

	@Override
	public List<Command> getCommands()
	{
		return Arrays.<Command>asList(flood);
	}

	@Override
	protected void configure()
	{
		bind(random.class).toInstance(this);

		bind(DeadListChecker.class);

		Multibinder<EventListener> eventListenerBinder = Multibinder.newSetBinder(binder(), EventListener.class);
		eventListenerBinder.addBinding().to(EventRandom.class);

		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder(), Command.class);
		commandBinder.addBinding().to(CommandFlood.class);
	}
}
