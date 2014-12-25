package net.rizon.moo.plugin.logging;

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
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.Event;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.Server;

class EventLogging extends Event
{
	@Override
	protected void initDatabases()
	{
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `log` (`created` DATE DEFAULT CURRENT_TIMESTAMP, `type`, `source`, `target`, `reason`);");
		Moo.db.executeUpdate("CREATE INDEX IF NOT EXISTS `log_created_idx` on `log` (`created`)");
		Moo.db.executeUpdate("CREATE INDEX IF NOT EXISTS `log_source_idx` on `log` (`source`)");
		Moo.db.executeUpdate("CREATE INDEX IF NOT EXISTS `log_target_idx` on `log` (`target`)");
		
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `wallops_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `date` DATETIME DEFAULT CURRENT_TIMESTAMP, `type`, `source`, `message`);");
	}

	@Override
	public void onPrivmsg(final String source, final String channel, final String message)
	{
		if (!Moo.conf.logChannelsContains(channel))
			return;

		DateFormat format = new SimpleDateFormat(logging.conf.date);
		Date now = new Date();
		File logPath = new File(logging.conf.path);
		File logFilePath = new File(logPath, logging.conf.filename.replace("%DATE%", format.format(now)));
		
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
			logging.log.log(Level.WARNING, "Unable to log", e);
		}
	}

	private static final Pattern killPattern = Pattern.compile("Received KILL message for ([^ ]+)\\. From ([^ ]+) Path: [^ ]+ \\((.*)\\)");

	@Override
	public void onNotice(final String source, final String channel, final String message)
	{
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

					Moo.db.executeUpdate();
				}
				catch (SQLException ex)
				{
					Logger.getGlobalLogger().log(ex);
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
				chan_list.addAll(Arrays.asList(Moo.conf.oper_channels));
				break;
			default:
				/* Default is admin_channels, which is handled below. */
				break;
		}

		/* admin_channels must always know. */
		chan_list.addAll(Arrays.asList(Moo.conf.admin_channels));

		for (final String chan : chan_list)
			Moo.privmsg(chan, "[" + type + "-LINE] " + serv.getName() + " " + message);
	}

	@Override
	public void OnXLineAdd(Server serv, char type, final String value)
	{
		NotifyXLineChanges(serv, type, "has a new " + type + "-Line for " + value + (type == 'O' ? " with flags " + serv.olines_work.get(value) : ""));

		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `source`, `target`, `reason`) VALUES (?, ?, ?, ?)");

			stmt.setString(1, type + "LINE");
			stmt.setString(2, serv.getName());
			stmt.setString(3, value);
			stmt.setString(4, "Added");

			Moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
		}
	}

	@Override
	public void OnXLineDel(Server serv, char type, final String value)
	{
		NotifyXLineChanges(serv, type, "removed " + type + "-Line for " + value);

		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `source`, `target`, `reason`) VALUES (?, ?, ?, ?)");

			stmt.setString(1, type + "LINE");
			stmt.setString(2, serv.getName());
			stmt.setString(3, value);
			stmt.setString(4, "Removed");

			Moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
		}
	}

	@Override
	public void OnOLineChange(final Server serv, final String oper, final String diff)
	{
		NotifyXLineChanges(serv, 'O', "changed flags for " + oper + ": " + diff);

		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `source`, `target`, `reason`) VALUES (?, ?, ?, ?)");

			stmt.setString(1, "OLINE");
			stmt.setString(2, serv.getName());
			stmt.setString(3, oper);
			stmt.setString(4, "Changed: " + diff);

			Moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
		}
	}

	@Override
	public void onServerLink(Server serv, Server to)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `source`, `target`) VALUES (?, ?, ?)");

			stmt.setString(1, "LINK");
			stmt.setString(2, serv.getName());
			stmt.setString(3, to.getName());

			Moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
		}
	}

	@Override
	public void onServerSplit(Server serv, Server from)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `source`, `target`) VALUES (?, ?, ?)");

			stmt.setString(1, "SPLIT");
			stmt.setString(2, serv.getName());
			stmt.setString(3, from.getName());

			Moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
		}
	}

	private static final Pattern akillAddPattern = Pattern.compile("([^ ]+) added an AKILL for [^@]+@([^ ]+) \\((.*)\\)\\]$");
	private static final Pattern akillDelPattern = Pattern.compile("([^ ]+) removed an AKILL for [^@]@([^ ]+) \\((.*) - .*\\)$");
	private static final Pattern operPattern = Pattern.compile("\2?([^!]+).*\2? is now an IRC operator");
	private static final Pattern sessionPattern = Pattern.compile("Added a temporary AKILL for \2[^@]+@([^ ]+)\2");
	private static final Pattern connectPattern = Pattern.compile("Remote CONNECT ([^ ]*) [0-9]* from ([^ ]*)$");
	private static final Pattern wallTypePattern = Pattern.compile("^([A-Z]+) - (.+)");
	
	private static void checkAkill(final String ip)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("SELECT count(*) FROM `log` WHERE `type` = 'AKILL' and `target` = ?");
			stmt.setString(1, ip);

			ResultSet rs = Moo.db.executeQuery();
			if (rs.next())
			{
				int count = rs.getInt("count(*)");
				if (count > 0 && count % 50 == 0)
					Moo.operwall(ip + " has been akilled " + count + " times - consider akilling it longer");
			}
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
		}
	}

	@Override
	public void onWallops(final String source, final String message)
	{
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

			Moo.db.executeUpdate();

			checkAkill(m.group(2));
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
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

			Moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
		}
	}
	
	private void operLog(Matcher m)
	{
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `target`) VALUES (?, ?)");

			stmt.setString(1, "OPER");
			stmt.setString(2, m.group(1));

			Moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
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

			Moo.db.executeUpdate();

			checkAkill(m.group(1));
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
		}
	}
	
	private void remoteConnectLog(Matcher m)
	{
		Server s = Server.findServer(m.group(1));
		if (s == null)
			return;

		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO log (`type`, `source`, `target`) VALUES (?, ?, ?)");

			stmt.setString(1, "CONNECT");
			stmt.setString(2, m.group(2));
			stmt.setString(3, s.getName());

			Moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
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

			Moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
		}
	}
}
