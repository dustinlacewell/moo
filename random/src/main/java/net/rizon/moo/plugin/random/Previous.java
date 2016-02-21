package net.rizon.moo.plugin.random;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.rizon.moo.Moo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Previous extends FloodList
{
	private static final Logger logger = LoggerFactory.getLogger(Previous.class);
	
	private static PreparedStatement stmt;
	private static Previous self;

	static
	{
		try
		{
			stmt = Moo.db.prepare("SELECT `date`,`count` FROM `akills` WHERE `ip` = ?");
		}
		catch (SQLException ex)
		{
			logger.error("Unable to load previous akills", ex);
		}
	}

	public Previous(random random)
	{
		super(random);
	}

	@Override
	public String toString()
	{
		return "PREVIOUS";
	}

	protected static FloodList matches(random random, NickData nd)
	{
		try
		{
			stmt.setString(1, nd.ip);
			ResultSet rs = stmt.executeQuery();
			boolean has = rs.next();
			rs.close();
			if (has)
			{
				if (self == null || self.isClosed)
				{
					/* My list has been closed (and thus detached from everything), so start a new list. */
					self = new Previous(random);
					self.open();
				}

				return self;
			}
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to check prevoius match", ex);
		}

		return null;
	}
}