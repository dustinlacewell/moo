package net.rizon.moo.irc;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import net.rizon.moo.Database;
import net.rizon.moo.Moo;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.events.InitDatabases;
import net.rizon.moo.events.LoadDatabases;
import net.rizon.moo.events.SaveDatabases;
import net.rizon.moo.util.Match;
import org.slf4j.Logger;

public class ServerManager implements EventListener
{
	@Inject
	private static Logger logger;
	
	public long lastSplit = 0;
	public int last_total_users = 0, cur_total_users = 0, work_total_users = 0;

	public Server root;

	private List<Server> servers = new LinkedList<>();
	public Date last_link = null, last_split = null;

	public Server findServer(String name)
	{
		for (Server s : servers)
		{
			if (Match.matches(s.getName(), "*" + name + "*"))
			{
				return s;
			}
		}
		return null;
	}

	public Server findServerAbsolute(String name)
	{
		for (Server s : servers)
		{
			if (s.getName().equalsIgnoreCase(name))
			{
				return s;
			}
		}
		return null;
	}

	public final Server[] getServers()
	{
		Server[] s = new Server[servers.size()];
		servers.toArray(s);
		return s;
	}

	@Subscribe
	public void initDatabases(InitDatabases evt)
	{
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS splits (`name` varchar(64), `from` varchar(64), `to` varchar(64), `when` date, `end` date, `reconnectedBy` varchar(64), `recursive`);");
		Moo.db.executeUpdate("CREATE INDEX IF NOT EXISTS `splits_idx` on `splits` (`name`,`when`,`from`)");
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS servers (`name`, `created` DATE DEFAULT CURRENT_TIMESTAMP, `desc`, `preferred_links`, `frozen`);");
		Moo.db.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS `servers_name_idx` on `servers` (`name`);");
	}

	@Subscribe
	public void loadDatabases(LoadDatabases evt)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("SELECT * FROM servers");
			ResultSet rs = Moo.db.executeQuery(stmt);
			while (rs.next())
			{
				String name = rs.getString("name"), desc = rs.getString("desc"), pl = rs.getString("preferred_links");
				Date created = rs.getDate("created");
				boolean frozen = rs.getBoolean("frozen");

				Server s = findServerAbsolute(name);
				if (s == null)
				{
					s = new Server(name);
				}
				else
				{
					s.allowed_clines.clear();
				}

				if (desc != null)
				{
					s.setDesc(desc);
				}
				s.created = created;
				for (String l : pl.split(" "))
				{
					if (l.trim().isEmpty() == false)
					{
						s.allowed_clines.add(l.trim());
					}
				}
				s.frozen = frozen;
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
		}
	}

	@Subscribe
	public void saveDatabases(SaveDatabases evt)
	{
		try
		{
			PreparedStatement statement;

			for (Server s : getServers())
			{
				statement = Moo.db.prepare("REPLACE INTO servers (`name`, `desc`, `preferred_links`, `frozen`) VALUES(?, ?, ?, ?)");
				statement.setString(1, s.getName());
				statement.setString(2, s.getDesc());
				String links = "";
				for (String string : s.allowed_clines)
				{
					links += string + " ";
				}
				links = links.trim();
				statement.setString(3, links);
				statement.setBoolean(4, s.frozen);

				Moo.db.executeUpdate(statement);
			}

		}
		catch (SQLException ex)
		{
			logger.warn("Error saving servers", ex);
		}
	}
}
