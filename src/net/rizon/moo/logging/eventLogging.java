package net.rizon.moo.logging;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.rizon.moo.event;
import net.rizon.moo.moo;
import net.rizon.moo.server;

public class eventLogging extends event
{
	@Override
	protected void initDatabases()
	{
		moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `log` (`created` DATE DEFAULT CURRENT_TIMESTAMP, `type`, `source`, `target`, `reason`);");
		moo.db.executeUpdate("CREATE INDEX IF NOT EXISTS `log_created_idx` on `log` (`created`)");
		moo.db.executeUpdate("CREATE INDEX IF NOT EXISTS `log_source_idx` on `log` (`source`)");
		moo.db.executeUpdate("CREATE INDEX IF NOT EXISTS `log_target_idx` on `log` (`target`)");
	}
	
	@Override
	public void OnXLineAdd(server serv, char type, final String value)
	{
		for (final String chan : moo.conf.getAdminChannels())
			moo.privmsg(chan, "[" + type + "-LINE] " + serv.getName() + " has a new " + type + "-Line for " + value);
		
		try
		{
			PreparedStatement stmt = moo.db.prepare("INSERT INTO log (`type`, `source`, `target`) VALUES (?, ?, ?)");
			
			stmt.setString(1, type + "LINE");
			stmt.setString(2, serv.getName());
			stmt.setString(3, value);
			
			moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
	}
}