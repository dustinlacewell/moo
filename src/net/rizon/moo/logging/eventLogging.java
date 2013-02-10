package net.rizon.moo.logging;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.rizon.moo.event;
import net.rizon.moo.moo;
import net.rizon.moo.server;

class eventLogging extends event
{
	@Override
	protected void initDatabases()
	{
		moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `log` (`created` DATE DEFAULT CURRENT_TIMESTAMP, `type`, `source`, `target`, `reason`);");
		moo.db.executeUpdate("CREATE INDEX IF NOT EXISTS `log_created_idx` on `log` (`created`)");
		moo.db.executeUpdate("CREATE INDEX IF NOT EXISTS `log_source_idx` on `log` (`source`)");
		moo.db.executeUpdate("CREATE INDEX IF NOT EXISTS `log_target_idx` on `log` (`target`)");
		
		moo.db.executeUpdate("CREATE TABLE `services_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `date` datetime default current_timestamp, `data` collate nocase)");
	}
	
	@Override
	public void onPrivmsg(final String source, final String channel, final String message)
	{
		if (moo.conf.isLogChannel(channel) == false)
			return;
		
		try
		{
			PreparedStatement stmt = moo.db.prepare("INSERT INTO `services_logs` (`data`) VALUES(?)");
				
			stmt.setString(1, message);
				
			moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	@Override
	public void OnXLineAdd(server serv, char type, final String value)
	{
		for (final String chan : moo.conf.getAdminChannels())
			moo.privmsg(chan, "[" + type + "-LINE] " + serv.getName() + " has a new " + type + "-Line for " + value + (type == 'O' ? " with flags " + serv.olines_work.get(value) : ""));
		
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
	
	@Override
	public void OnXLineDel(server serv, char type, final String value)
	{
		for (final String chan : moo.conf.getAdminChannels())
			moo.privmsg(chan, "[" + type + "-LINE] " + serv.getName() + " removed " + type + "-Line for " + value);
	}
	
	@Override
	public void OnOLineChange(final server serv, final String oper, final String diff)
	{
		for (final String chan : moo.conf.getAdminChannels())
			moo.privmsg(chan, "[O-LINE] " + serv.getName() + " changed flags for " + oper + ": " + diff);
	}
	
	@Override
	public void onServerLink(server serv, server to)
	{
		try
		{
			PreparedStatement stmt = moo.db.prepare("INSERT INTO log (`type`, `source`, `target`) VALUES (?, ?, ?)");
			
			stmt.setString(1, "LINK");
			stmt.setString(2, serv.getName());
			stmt.setString(3, to.getName());
			
			moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
	}
	
	@Override
	public void onServerSplit(server serv, server from)
	{
		try
		{
			PreparedStatement stmt = moo.db.prepare("INSERT INTO log (`type`, `source`, `target`) VALUES (?, ?, ?)");
			
			stmt.setString(1, "SPLIT");
			stmt.setString(2, serv.getName());
			stmt.setString(3, from.getName());
			
			moo.db.executeUpdate();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
		}
	}
}