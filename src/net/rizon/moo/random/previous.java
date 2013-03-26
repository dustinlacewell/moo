package net.rizon.moo.random;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.rizon.moo.moo;

class previous extends floodList
{
	private static PreparedStatement stmt;
	private static previous self = new previous();
	
	static
	{
		try
		{
			stmt = moo.db.prepare("SELECT `date`,`count` FROM `akills` WHERE `ip` = ?");
			moo.db.detach();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	@Override
	public String toString()
	{
		return "PREVIOUS";
	}
	
	protected static floodList matches(nickData nd)
	{
		if (self.isClosed)
		{
			/* My list has been closed (and thus detached from everything), so start a new list. */
			self = new previous();
		}

		try
		{
			stmt.setString(1, nd.ip);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				return self;
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}

		return null;
	}
}