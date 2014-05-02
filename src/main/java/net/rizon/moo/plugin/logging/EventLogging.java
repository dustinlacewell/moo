package net.rizon.moo.plugin.logging;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
		
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `services_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `date` datetime default current_timestamp, `data` collate nocase)");
		Moo.db.executeUpdate("DELETE FROM `services_logs` WHERE `date` < date('now', '-30 day')");
	}
	
	private int count = 0;
	
	@Override
	public void saveDatabases()
	{
		if (++count == 144)
		{
			Moo.db.executeUpdate("DELETE FROM `services_logs` WHERE `date` < date('now', '-30 day')");
			count = 0;
		}
	}
	
	@Override
	public void onPrivmsg(final String source, final String channel, final String message)
	{
		if (Moo.conf.listContains("log_channels", channel) == false)
			return;
		
		try
		{
			PreparedStatement stmt = Moo.db.prepare("INSERT INTO `services_logs` (`data`) VALUES(?)");
				
			stmt.setString(1, message);
				
			Moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
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
	
	@Override
	public void OnXLineAdd(Server serv, char type, final String value)
	{
		for (final String chan : Moo.conf.getList("admin_channels"))
			Moo.privmsg(chan, "[" + type + "-LINE] " + serv.getName() + " has a new " + type + "-Line for " + value + (type == 'O' ? " with flags " + serv.olines_work.get(value) : ""));
		
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
		for (final String chan : Moo.conf.getList("admin_channels"))
			Moo.privmsg(chan, "[" + type + "-LINE] " + serv.getName() + " removed " + type + "-Line for " + value);

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
		for (final String chan : Moo.conf.getList("admin_channels"))
			Moo.privmsg(chan, "[O-LINE] " + serv.getName() + " changed flags for " + oper + ": " + diff);

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
	
	private static final Pattern akillAddPattern = Pattern.compile("([^ ]+) added an AKILL for [^@]+@([^ ]+) \\((.*)[)\\]]$");
	private static final Pattern akillDelPattern = Pattern.compile("([^ ]+) removed an AKILL for [^@]@([^ ]+) \\((.*) - .*\\)$");
	private static final Pattern operPattern = Pattern.compile("\2([^ ]+)\2 is now an IRC operator");
	private static final Pattern sessionPattern = Pattern.compile("Added a temporary AKILL for \2[^@]+@([^ ]+)\2");
	private static final Pattern connectPattern = Pattern.compile("Remote CONNECT ([^ ]*) [0-9]* from ([^ ]*)$");
	
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
		if (message.startsWith("OPERWALL") == false && source.indexOf('@') != -1)
			return;
		
		Matcher m = akillAddPattern.matcher(message);
		if (m.find())
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
			
			return;
		}
		
		m = akillDelPattern.matcher(message);
		if (m.find())
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
			
			return;
		}
		
		m = operPattern.matcher(message);
		if (m.find())
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
			
			return;
		}
		
		m = sessionPattern.matcher(message);
		if (m.find())
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
			
			return;
		}
		
		m = connectPattern.matcher(message);
		if (m.find())
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
			
			return;
		}
	}
}
