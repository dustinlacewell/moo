package net.rizon.moo.plugin.logging;

import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.logging.conf.LoggingConfiguration;
import org.slf4j.Logger;

public class logging extends Plugin
{
	@Inject
	private static Logger logger;

	@Inject
	private CommandLogSearch logsearch;

	@Inject
	private CommandSLogSearch slogsearch;

	@Inject
	private CommandWLogSearch wlogsearch;

	private LoggingConfiguration conf;

	public logging() throws Exception
	{
		super("Logging", "Search server logs");
		conf = LoggingConfiguration.load();
	}

	@Override
	public void start() throws Exception
	{
	}

	@Override
	public void stop()
	{
	}

	public static void addEntry(String type, String source, String target, String reason)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `source`, `target`, `reason`) VALUES (?, ?, ?, ?)");

			stmt.setString(1, type == null ? "" : type);
			stmt.setString(2, source == null ? "" : source);
			stmt.setString(3, target == null ? "" : target);
			stmt.setString(4, reason == null ? "" : reason);

			Moo.db.executeUpdate(stmt);
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to log log entry", ex);
		}
	}

	@Override
	public List<Command> getCommands()
	{
		return Arrays.asList(logsearch, slogsearch, wlogsearch);
	}

	@Override
	protected void configure()
	{
		bind(logging.class).toInstance(this);

		bind(LoggingConfiguration.class).toInstance(conf);

		Multibinder<EventListener> eventListenerBinder = Multibinder.newSetBinder(binder(), EventListener.class);
		eventListenerBinder.addBinding().to(EventLogging.class);

		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder(), Command.class);
		commandBinder.addBinding().to(CommandLogSearch.class);
		commandBinder.addBinding().to(CommandWLogSearch.class);
		commandBinder.addBinding().to(CommandSLogSearch.class);
	}
}
