package net.rizon.moo.plugin.mxbl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

/**
 * Manages blacklisted MX records.
 * <p>
 * @author Orillion <orillion@rizon.net>
 */
public class mxbl extends Plugin
{
	private Command blacklist;
	private Event register;

	public mxbl() throws Exception
	{
		super("MXBL", "Monitors and suspends nicknames registered with blacklisted mailservers.");

		// Enable foreign keys if not enabled by default.
		// TODO: Should really enable this by default.
		Moo.db.executeUpdate("PRAGMA foreign_keys = ON");

		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS mxbl_wildcard ("
				+ "`id` INTEGER PRIMARY KEY,"
				+ "`wildcard` VARCHAR(128) UNIQUE COLLATE NOCASE,"
				+ "`oper` VARCHAR(64),"
				+ "`created` INTEGER)");

		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS mxbl ("
				+ "`id` INTEGER PRIMARY KEY,"
				+ "`wildcard_id` INTEGER REFERENCES mxbl_wildcard(id) ON DELETE CASCADE,"
				+ "`host` VARCHAR(128) COLLATE NOCASE,"
				+ "`oper` VARCHAR(64),"
				+ "`created` INTEGER)");

		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS mxbl_ips ("
				+ "`host` INTEGER,"
				+ "`ip` VARCHAR(64),"
				+ "FOREIGN KEY(host) REFERENCES mxbl(id) ON DELETE CASCADE)");
	}

	@Override
	public void start() throws Exception
	{
		PreparedStatement ps = Moo.db.prepare("SELECT * FROM `mxbl_wildcard`");
		ResultSet rs = ps.executeQuery();

		while (rs.next())
		{
			PreparedStatement stmt;
			ResultSet rset;
			MailhostWildcard mw = new MailhostWildcard(rs);
			stmt = Moo.db.prepare("SELECT * FROM `mxbl` WHERE `wildcard_id` = ?");
			stmt.setInt(1, rs.getInt("id"));
			rset = stmt.executeQuery();
			while (rset.next())
			{
				buildMailhosts(rset, mw);
			}
		}

		ps = Moo.db.prepare("SELECT * FROM `mxbl` WHERE `wildcard_id` IS NULL");
		rs = ps.executeQuery();

		while (rs.next())
		{
			buildMailhosts(rs, null);
		}

		blacklist = new CommandBlacklist(this);
		register = new EventRegister();
	}

	private void buildMailhosts(ResultSet rs, MailhostWildcard mw) throws SQLException
	{
		Mailhost m = new Mailhost(rs, mw);
		if (mw != null)
		{
			mw.addHost(m);
		}
		PreparedStatement stmt = Moo.db.prepare("SELECT `ip` FROM `mxbl_ips` WHERE `host` = ?");
		stmt.setInt(1, rs.getInt("id"));
		ResultSet rset = stmt.executeQuery();
		while (rset.next())
		{
			m.addIP(rset.getString("ip"));
		}
	}

	@Override
	public void stop()
	{
		if (blacklist != null)
		{
			blacklist.remove();
		}
		if (register != null)
		{
			register.remove();
		}
	}

}
