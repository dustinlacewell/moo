package net.rizon.moo.logging;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.message;
import net.rizon.moo.moo;

public class messageNotice extends message
{
	private static final Pattern killPattern = Pattern.compile("Received KILL message for ([^ ]+)\\. From ([^ ]+) Path: [^ ]+ \\((.*)\\)");
			
	public messageNotice()
	{
		super("NOTICE");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (source.indexOf('.') != -1)
		{
			Matcher m = killPattern.matcher(message[1]);
			if (m.find())
			{
				try
				{
					PreparedStatement stmt = moo.db.prepare("INSERT INTO log (`type`, `source`, `target`, `reason`) VALUES (?, ?, ?, ?)");
					
					stmt.setString(1, "KILL");
					stmt.setString(2, m.group(2));
					stmt.setString(3, m.group(1));
					stmt.setString(4, m.group(3));
					
					moo.db.executeUpdate();
				}
				catch (SQLException ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}	
}