package net.rizon.moo.logging;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.server;

public class messageWallops extends message
{
	private static final Pattern akillPattern = Pattern.compile("([^ ]+) added an AKILL for [^@]+@([^ ]+) \\((.*)[)\\]]$");
	private static final Pattern operPattern = Pattern.compile("\2([^ ]+)\2 is now an IRC operator");
	private static final Pattern sessionPattern = Pattern.compile("Added a temporary AKILL for \2[^@]+@([^ ]+)\2");
	private static final Pattern connectPattern = Pattern.compile("Remote CONNECT ([^ ]*) [0-9]* from ([^ ]*)$");
	
	private static void checkAkill(final String ip)
	{
		try
		{
			PreparedStatement stmt = moo.db.prepare("SELECT count(*) FROM `log` WHERE `type` = 'AKILL' and `target` = ?");
			stmt.setString(1, ip);
			
			ResultSet rs = moo.db.executeQuery();
			if (rs.next())
			{
				int count = rs.getInt("count(*)");
				if (count > 0 && count % 50 == 0)
					moo.operwall(ip + " has been akilled " + count + " times - consider akilling it longer");
			}
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	public messageWallops()
	{
		super("WALLOPS");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message[0].startsWith("OPERWALL") == false && source.indexOf('.') == -1)
			return;
		
		Matcher m = akillPattern.matcher(message[0]);
		if (m.find())
		{
			try
			{
				PreparedStatement stmt = moo.db.prepare("INSERT INTO log (`type`, `source`, `target`, `reason`) VALUES (?, ?, ?, ?)");
				
				stmt.setString(1, "AKILL");
				stmt.setString(2, m.group(1));
				stmt.setString(3, m.group(2));
				stmt.setString(4, m.group(3));
				
				moo.db.executeUpdate();
				
				checkAkill(m.group(2));
			}
			catch (SQLException ex)
			{
				ex.printStackTrace();
			}
			
			return;
		}
		
		m = operPattern.matcher(message[0]);
		if (m.find())
		{
			try
			{
				PreparedStatement stmt = moo.db.prepare("INSERT INTO log (`type`, `target`) VALUES (?, ?)");
				
				stmt.setString(1, "OPER");
				stmt.setString(2, m.group(1));
				
				moo.db.executeUpdate();
			}
			catch (SQLException ex)
			{
				ex.printStackTrace();
			}
			
			return;
		}
		
		m = sessionPattern.matcher(message[0]);
		if (m.find())
		{
			try
			{
				PreparedStatement stmt = moo.db.prepare("INSERT INTO log (`type`, `source`, `target`, `reason`) VALUES (?, ?, ?, ?)");
				
				stmt.setString(1, "AKILL");
				stmt.setString(2, "OperServ");
				stmt.setString(3, m.group(1));
				stmt.setString(4, "Session limit exceeded");
				
				moo.db.executeUpdate();
				
				checkAkill(m.group(1));
			}
			catch (SQLException ex)
			{
				ex.printStackTrace();
			}
			
			return;
		}
		
		m = connectPattern.matcher(message[0]);
		if (m.find())
		{
			server s = server.findServer(m.group(1));
			if (s == null)
				return;
			
			try
			{
				PreparedStatement stmt = moo.db.prepare("INSERT INTO log (`type`, `source`, `target`) VALUES (?, ?, ?)");
				
				stmt.setString(1, "CONNECT");
				stmt.setString(2, m.group(2));
				stmt.setString(3, s.getName());
				
				moo.db.executeUpdate();
			}
			catch (SQLException ex)
			{
				ex.printStackTrace();
			}
			
			return;
		}
	}
}