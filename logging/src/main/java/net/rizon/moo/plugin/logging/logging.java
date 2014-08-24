package net.rizon.moo.plugin.logging;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class logging extends Plugin
{
	private Command ls, sls;
	private Event e;

	public logging()
	{
		super("Logging", "Search server logs");
	}

	@Override
	public void start() throws Exception
	{
		ls = new CommandLogSearch(this);
		sls = new CommandSLogSearch(this);

		e = new EventLogging();
	}

	@Override
	public void stop()
	{
		ls.remove();
		sls.remove();

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

			Moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
		}
	}
}
