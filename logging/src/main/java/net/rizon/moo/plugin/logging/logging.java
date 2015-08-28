package net.rizon.moo.plugin.logging;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.logging.conf.LoggingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class logging extends Plugin
{
	private static final Logger logger = LoggerFactory.getLogger(logging.class);
	
	private Command ls, sls, wls;
	private Event e;
	public static LoggingConfiguration conf;

	public logging() throws Exception
	{
		super("Logging", "Search server logs");
		conf = LoggingConfiguration.load();
	}

	@Override
	public void start() throws Exception
	{
		ls = new CommandLogSearch(this);
		sls = new CommandSLogSearch(this);
		wls = new CommandWLogSearch(this);

		e = new EventLogging();
	}

	@Override
	public void stop()
	{
		ls.remove();
		sls.remove();
		wls.remove();

		e.remove();
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
}
