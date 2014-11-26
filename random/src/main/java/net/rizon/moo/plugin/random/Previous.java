package net.rizon.moo.plugin.random;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.rizon.moo.Logger;
import net.rizon.moo.Moo;

class Previous extends FloodList
{
	private static PreparedStatement stmt;
	private static Previous self;

	static
	{
		try
		{
			stmt = Moo.db.prepare("SELECT `date`,`count` FROM `akills` WHERE `ip` = ?");
			Moo.db.detach();
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
		}
	}

	@Override
	public String toString()
	{
		return "PREVIOUS";
	}

	protected static FloodList matches(NickData nd)
	{
		try
		{
			stmt.setString(1, nd.ip);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
			{
				if (self == null || self.isClosed)
				{
					/* My list has been closed (and thus detached from everything), so start a new list. */
					self = new Previous();
					self.open();
				}

				return self;
			}
		}
		catch (SQLException ex)
		{
			Logger.getGlobalLogger().log(ex);
		}

		return null;
	}
}