package net.rizon.moo.irc;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import io.netty.util.concurrent.ScheduledFuture;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.rizon.moo.Database;
import net.rizon.moo.Moo;
import net.rizon.moo.Split;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.events.InitDatabases;
import net.rizon.moo.events.LoadDatabases;
import net.rizon.moo.events.OnServerCreate;
import net.rizon.moo.events.OnServerDestroy;
import net.rizon.moo.events.SaveDatabases;
import net.rizon.moo.util.Match;
import org.slf4j.Logger;

public class ServerManager implements EventListener
{
	@Inject
	private static Logger logger;

	@Inject
	private EventBus eventBus;

	@Inject
	private Protocol protocol;
	
	public int last_total_users = 0, cur_total_users = 0, work_total_users = 0;

	public Server root;

	private final List<Server> servers = new LinkedList<>();
	public Date last_link = null, last_split = null;

	private final Map<Server, ScheduledFuture> stats = new HashMap<>();

	public void insertServer(final Server server)
	{
		servers.add(server);

		logger.debug("Adding server {}", server.getName());

		requestStats(server);
		if (!server.isServices())
			protocol.write("VERSION", server.getName());
		protocol.write("MAP");

		eventBus.post(new OnServerCreate(server));

		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				if (!server.isServices())
					requestStats(server);

				protocol.write("MAP");
			}
		};
		ScheduledFuture sf = Moo.scheduleWithFixedDelay(r, 5, TimeUnit.MINUTES);
		stats.put(server, sf);
	}

	public void removeServer(Server server)
	{
		logger.debug("Removing server {}", server.getName());

		eventBus.post(new OnServerDestroy(server));

		try
		{
			PreparedStatement statement = Moo.db.prepare("DELETE FROM servers WHERE `name` = ?");
			statement.setString(1, server.getName());
			Moo.db.executeUpdate(statement);

			statement = Moo.db.prepare("DELETE FROM splits WHERE `name` = ?");
			statement.setString(1, server.getName());
			Moo.db.executeUpdate(statement);
		}
		catch (SQLException ex)
		{
			logger.error("Error removing server from database", ex);
		}

		stats.get(server).cancel(true);
		servers.remove(server);
	}

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
					this.insertServer(s);
				}
				else
				{
					s.allowed_clines.clear();
				}

				if (desc != null)
				{
					s.setDesc(desc);
				}
				s.setCreated(created);
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

	public void requestStats(Server server)
	{
		protocol.write("STATS", "c", server.getName());
		protocol.write("STATS", "o", server.getName());
	}
	
	public void split(Server server, Server from)
	{
		server.split(from);
		
		Date now = new Date();
		
		// Find servers that split from this one at the same time
		for (Server serv : getServers())
		{
			Split sp = serv.getSplit();
			if (sp != null && sp.from.equals(server.getName()) && sp.when.getTime() / 1000L == now.getTime() / 1000L)
			{
				sp.recursive = true;

				try
				{
					PreparedStatement statement = Moo.db.prepare("UPDATE splits SET `recursive` = ? WHERE `name` = ? AND `from` = ? AND `when` = ?");
					statement.setBoolean(1, sp.recursive);
					statement.setString(2, sp.me);
					statement.setString(3, sp.from);
					statement.setDate(4, new java.sql.Date(sp.when.getTime()));
					Moo.db.executeUpdate(statement);
				}
				catch (SQLException ex)
				{
					Database.handleException(ex);
				}
			}
		}
	}
}
