package net.rizon.moo.plugin.logging;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.Moo;
import net.rizon.moo.conf.Config;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.events.EventNotice;
import net.rizon.moo.events.EventPrivmsg;
import net.rizon.moo.events.EventWallops;
import net.rizon.moo.events.InitDatabases;
import net.rizon.moo.events.OnOLineChange;
import net.rizon.moo.events.OnServerLink;
import net.rizon.moo.events.OnServerSplit;
import net.rizon.moo.events.OnXLineAdd;
import net.rizon.moo.events.OnXLineDel;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;
import net.rizon.moo.plugin.logging.conf.LoggingConfiguration;
import org.slf4j.Logger;

class EventLogging implements EventListener
{
	@Inject
	private static Logger logger;

	@Inject
	private Config conf;

	@Inject
	private LoggingConfiguration logconf;

	@Inject
	private Protocol protocol;

	@Inject
	private ServerManager serverManager;

	@Subscribe
	public void initDatabases(InitDatabases evt)
	{
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `log` (`created` DATE DEFAULT CURRENT_TIMESTAMP, `type`, `source`, `target`, `reason`);");
		Moo.db.executeUpdate("CREATE INDEX IF NOT EXISTS `log_created_idx` on `log` (`created`)");
		Moo.db.executeUpdate("CREATE INDEX IF NOT EXISTS `log_source_idx` on `log` (`source`)");
		Moo.db.executeUpdate("CREATE INDEX IF NOT EXISTS `log_target_idx` on `log` (`target`)");
		
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `wallops_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `date` DATETIME DEFAULT CURRENT_TIMESTAMP, `type`, `source`, `message`);");
	}

	@Subscribe
	public void onPrivmsg(EventPrivmsg evt)
	{
		String channel = evt.getChannel(), message = evt.getMessage();
		
		if (!conf.logChannelsContains(channel))
			return;

		DateFormat format = new SimpleDateFormat(logconf.date);
		Date now = new Date();
		File logPath = new File(logconf.path);
		File logFilePath = new File(logPath, logconf.filename.replace("%DATE%", format.format(now)));
		
		logPath.mkdirs();
		
		try
		{
			FileWriter out = new FileWriter(logFilePath, true);
			try
			{
				out.write(now + ": " + message + "\n");
			}
			finally
			{
				out.close();
			}
		}
		catch (Exception e)
		{
			logger.warn("Unable to log", e);
		}
	}

	private static final Pattern killPattern = Pattern.compile("Received KILL message for ([^ ]+)\\. From ([^ ]+) Path: [^ ]+ \\((.*)\\)");

	@Subscribe
	public void onNotice(EventNotice evt)
	{
		String source = evt.getSource(), message = evt.getMessage();
		
		if (source.indexOf('.') != -1)
		{
			Matcher m = killPattern.matcher(message);
			if (m.find())
			{
				try
				{
					PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `source`, `target`, `reason`) VALUES (?, ?, ?, ?)");

					stmt.setString(1, "KILL");
					stmt.setString(2, m.group(2));
					stmt.setString(3, m.group(1));
					stmt.setString(4, m.group(3));

					Moo.db.executeUpdate(stmt);
				}
				catch (SQLException ex)
				{
					logger.warn("Unable to log kill", ex);
				}
			}
		}
	}

	private void NotifyXLineChanges(Server serv, char type, final String message)
	{
		HashSet<String> chan_list = new HashSet<String>();

		switch (type)
		{
			case 'O':
				chan_list.addAll(Arrays.asList(conf.oper_channels));
				break;
			default:
				/* Default is admin_channels, which is handled below. */
				break;
		}

		/* admin_channels must always know. */
		chan_list.addAll(Arrays.asList(conf.admin_channels));

		for (final String chan : chan_list)
			protocol.privmsg(chan, "[" + type + "-LINE] " + serv.getName() + " " + message);
	}

	@Subscribe
	public void OnXLineAdd(OnXLineAdd evt)
	{
		Server serv = evt.getServer();
		char type = evt.getType();
		String value = evt.getValue();
		
		NotifyXLineChanges(serv, type, "has a new " + type + "-Line for " + value + (type == 'O' ? " with flags " + serv.olines_work.get(value) : ""));

		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `source`, `target`, `reason`) VALUES (?, ?, ?, ?)");

			stmt.setString(1, type + "LINE");
			stmt.setString(2, serv.getName());
			stmt.setString(3, value);
			stmt.setString(4, "Added");

			Moo.db.executeUpdate(stmt);
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to log xline", ex);
		}
	}

	@Subscribe
	public void OnXLineDel(OnXLineDel evt)
	{
		Server serv = evt.getServer();
		char type = evt.getType();
		String value = evt.getValue();
		
		NotifyXLineChanges(serv, type, "removed " + type + "-Line for " + value);

		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `source`, `target`, `reason`) VALUES (?, ?, ?, ?)");

			stmt.setString(1, type + "LINE");
			stmt.setString(2, serv.getName());
			stmt.setString(3, value);
			stmt.setString(4, "Removed");

			Moo.db.executeUpdate(stmt);
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to log xline", ex);
		}
	}

	@Subscribe
	public void OnOLineChange(OnOLineChange evt)
	{
		Server serv = evt.getServer();
		String oper = evt.getOper();
		String diff = evt.getDiff();
		
		NotifyXLineChanges(serv, 'O', "changed flags for " + oper + ": " + diff);

		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `source`, `target`, `reason`) VALUES (?, ?, ?, ?)");

			stmt.setString(1, "OLINE");
			stmt.setString(2, serv.getName());
			stmt.setString(3, oper);
			stmt.setString(4, "Changed: " + diff);

			Moo.db.executeUpdate(stmt);
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to log xline", ex);
		}
	}

	@Subscribe
	public void onServerLink(OnServerLink evt)
	{
		Server serv = evt.getServer(), to = evt.getTo();
		
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `source`, `target`) VALUES (?, ?, ?)");

			stmt.setString(1, "LINK");
			stmt.setString(2, serv.getName());
			stmt.setString(3, to.getName());

			Moo.db.executeUpdate(stmt);
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to log server link", ex);
		}
	}

	@Subscribe
	public void onServerSplit(OnServerSplit evt)
	{
		Server serv = evt.getServer(), from = evt.getFrom();
		
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `source`, `target`) VALUES (?, ?, ?)");

			stmt.setString(1, "SPLIT");
			stmt.setString(2, serv.getName());
			stmt.setString(3, from.getName());

			Moo.db.executeUpdate(stmt);
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to log server split", ex);
		}
	}

	private static final Pattern akillAddPattern = Pattern.compile("([^ ]+) added an AKILL for [^@]+@([^ ]+) \\((.*)\\)\\]$");
	private static final Pattern akillDelPattern = Pattern.compile("([^ ]+) removed an AKILL for [^@]@([^ ]+) \\((.*) - .*\\)$");
	private static final Pattern operPattern = Pattern.compile("\2?([^!]+).*\2? is now an IRC operator");
	private static final Pattern sessionPattern = Pattern.compile("Added a temporary AKILL for \2[^@]+@([^ ]+)\2");
	private static final Pattern connectPattern = Pattern.compile("Remote CONNECT ([^ ]*) [0-9]* from ([^ ]*)$");
	private static final Pattern wallTypePattern = Pattern.compile("^([A-Z]+) - (.+)");
	
	private void checkAkill(final String ip)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("SELECT count(*) FROM `log` WHERE `type` = 'AKILL' and `target` = ?");
			stmt.setString(1, ip);

			ResultSet rs = Moo.db.executeQuery(stmt);
			if (rs.next())
			{
				int count = rs.getInt("count(*)");
				if (count > 0 && count % 50 == 0)
					protocol.operwall(ip + " has been akilled " + count + " times - consider akilling it longer");
			}
			
			rs.close();
			stmt.close();
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to check akill", ex);
		}
	}

	@Subscribe
	public void onWallops(EventWallops evt)
	{
		String source = evt.getSource(), message = evt.getMessage();
		
		wallopsLog(source, message);
		
		if (message.startsWith("OPERWALL") == false && source.indexOf('@') != -1)
			return;

		Matcher m = akillAddPattern.matcher(message);
		if (m.find())
		{
			addAkillLog(m);
			return;
		}

		m = akillDelPattern.matcher(message);
		if (m.find())
		{
			delAkillLog(m);
			return;
		}

		m = operPattern.matcher(message);
		if (m.find())
		{
			operLog(m);
			return;
		}

		m = sessionPattern.matcher(message);
		if (m.find())
		{
			addTempAkillLog(m);
			return;
		}

		m = connectPattern.matcher(message);
		if (m.find())
		{
			remoteConnectLog(m);
			return;
		}
	}
	
	private void addAkillLog(Matcher m)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `source`, `target`, `reason`) VALUES (?, ?, ?, ?)");

			stmt.setString(1, "AKILL");
			stmt.setString(2, m.group(1));
			stmt.setString(3, m.group(2));
			stmt.setString(4, m.group(3));

			Moo.db.executeUpdate(stmt);

			checkAkill(m.group(2));
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to log akill", ex);
		}
	}
	
	private void delAkillLog(Matcher m)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `source`, `target`, `reason`) VALUES (?, ?, ?, ?)");

			stmt.setString(1, "AKILLDEL");
			stmt.setString(2, m.group(1));
			stmt.setString(3, m.group(2));
			stmt.setString(4, m.group(3));

			Moo.db.executeUpdate(stmt);
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to log akill del", ex);
		}
	}
	
	private void operLog(Matcher m)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `target`) VALUES (?, ?)");

			stmt.setString(1, "OPER");
			stmt.setString(2, m.group(1));

			Moo.db.executeUpdate(stmt);
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to log oper", ex);
		}
	}
	
	private void addTempAkillLog(Matcher m)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `source`, `target`, `reason`) VALUES (?, ?, ?, ?)");

			stmt.setString(1, "AKILL");
			stmt.setString(2, "OperServ");
			stmt.setString(3, m.group(1));
			stmt.setString(4, "Session limit exceeded");

			Moo.db.executeUpdate(stmt);

			checkAkill(m.group(1));
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to log akill", ex);
		}
	}
	
	private void remoteConnectLog(Matcher m)
	{
		Server s = serverManager.findServer(m.group(1));
		if (s == null)
			return;

		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `source`, `target`) VALUES (?, ?, ?)");

			stmt.setString(1, "CONNECT");
			stmt.setString(2, m.group(2));
			stmt.setString(3, s.getName());

			Moo.db.executeUpdate(stmt);
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to log connect", ex);
		}
	}
	
	private void wallopsLog(final String source, final String message)
	{
		Matcher m = wallTypePattern.matcher(message);
		String type;
		String msg;
		
		if (m.find())
		{
			type = m.group(1);
			msg = m.group(2);
		}
		else
		{
			type = "WALLOPS";
			msg = message;
		}
		
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO wallops_logs (`type`, `source`, `message`) VALUES (?, ?, ?)");

			stmt.setString(1, type);
			stmt.setString(2, source);
			stmt.setString(3, msg);

			Moo.db.executeUpdate(stmt);
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to log wallops", ex);
		}
	}
}
