package net.rizon.moo.plugin.mxbl;

import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.EventListener;

/**
 * Manages blacklisted MX records.
 * <p>
 * @author Orillion <orillion@rizon.net>
 */
public class mxbl extends Plugin
{
	@Inject
	private CommandBlacklist blacklist;

	public mxbl() throws Exception
	{
		super("MXBL", "Monitors and suspends nicknames registered with blacklisted mailservers.");

		// Enable foreign keys if not enabled by default.
		// TODO: Should really enable this by default.
		Moo.db.executeUpdate("PRAGMA foreign_keys = ON");

		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS mxbl ("
			+ "`id` INTEGER PRIMARY KEY,"
			+ "`parent_id` INTEGER REFERENCES mxbl(id) ON DELETE CASCADE,"
			+ "`host` VARCHAR(128) COLLATE NOCASE,"
			+ "`oper` VARCHAR(64),"
			+ "`wildcard` TINYINT(1),"
			+ "`created` LONG)");

		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS mxbl_ips ("
			+ "`host` INTEGER,"
			+ "`ip` VARCHAR(64),"
			+ "FOREIGN KEY(host) REFERENCES mxbl(id) ON DELETE CASCADE)");
	}

	@Override
	public void start() throws Exception
	{
		PreparedStatement ps;
		ResultSet rs;

		// Load parent objects first.
		ps = Moo.db.prepare("SELECT * FROM `mxbl` WHERE `parent_id` IS NULL");
		rs = ps.executeQuery();

		while (rs.next())
		{
			buildMailhosts(rs, null);
		}
		
		rs.close();
		ps.close();

		// Load child objects next.
		ps = Moo.db.prepare("SELECT * FROM `mxbl` WHERE `parent_id` IS NOT NULL");
		rs = ps.executeQuery();

		while (rs.next())
		{
			Mailhost owner = Mailhost.getMailhost(rs.getInt("parent_id"));
			buildMailhosts(rs, owner);
		}
		
		rs.close();
		ps.close();
	}

	private void buildMailhosts(ResultSet rs, Mailhost mw) throws SQLException
	{
		Mailhost m = new Mailhost(rs, mw);

		PreparedStatement stmt = Moo.db.prepare("SELECT `ip` FROM `mxbl_ips` WHERE `host` = ?");
		stmt.setInt(1, rs.getInt("id"));
		ResultSet rset = stmt.executeQuery();
		while (rset.next())
		{
			m.addIP(rset.getString("ip"));
		}
		
		rset.close();
		stmt.close();
	}

	@Override
	public void stop()
	{
	}

	@Override
	public List<Command> getCommands()
	{
		return Arrays.<Command>asList(blacklist);
	}

	@Override
	protected void configure()
	{
		bind(mxbl.class).toInstance(this);

		Multibinder<EventListener> eventListenerBinder = Multibinder.newSetBinder(binder(), EventListener.class);
		eventListenerBinder.addBinding().to(EventRegister.class);

		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder(), Command.class);
		commandBinder.addBinding().to(CommandBlacklist.class);
	}

}
