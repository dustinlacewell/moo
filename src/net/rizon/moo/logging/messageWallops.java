package net.rizon.moo.logging;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.message;
import net.rizon.moo.moo;

public class messageWallops extends message
{
	private static final Pattern akillPattern = Pattern.compile("([^ ]+) added an AKILL for [^@]+@([^ ]+) \\((.*)\\)?");
	private static final Pattern operPattern = Pattern.compile("\2([^ ]+)\2 is now an IRC operator");
	private static final Pattern sessionPattern = Pattern.compile("Added a temporary AKILL for \2[^@]+@([^ ]+)\2");
	
	public messageWallops()
	{
		super("WALLOPS");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message[0].startsWith("OPERWALL") == false)
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
			}
			catch (SQLException ex)
			{
				ex.printStackTrace();
			}
			
			return;
		}
	}
}