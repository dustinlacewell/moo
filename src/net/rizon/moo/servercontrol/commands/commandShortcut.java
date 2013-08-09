package net.rizon.moo.servercontrol.commands;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.servercontrol.connection;
import net.rizon.moo.servercontrol.echoProcess;
import net.rizon.moo.servercontrol.process;
import net.rizon.moo.servercontrol.protocol;
import net.rizon.moo.servercontrol.serverInfo;
import net.rizon.moo.servercontrol.servercontrol;

public class commandShortcut extends command
{
	public commandShortcut(mpackage pkg)
	{
		super(pkg, "!S", "Configure and execute shortcuts");
		this.requiresChannel(moo.conf.getAdminChannels());
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length > 1 && params[1].equalsIgnoreCase("list"))
		{
			try
			{
				moo.db.prepare("SELECT name FROM shortcuts");
				ResultSet rs = moo.db.executeQuery();
				
				String shortcuts = "";
				while (rs.next())
					shortcuts += ", " + rs.getString("name");
				
				if (shortcuts.isEmpty())
					moo.reply(source, target, "There are no configured shortcuts");
				else
					moo.reply(source, target, "Shortcuts: " + shortcuts.substring(2));
			}
			catch (SQLException ex)
			{
				logger.getGlobalLogger().log(ex);
			}
		}
		else if (params.length > 3 && params[1].equalsIgnoreCase("add"))
		{
			try
			{
				PreparedStatement stmt = moo.db.prepare("DELETE FROM shortcuts WHERE `name` = ?");
				stmt.setString(1, params[2]);
				boolean replaced = moo.db.executeUpdate() != 0;
				
				stmt = moo.db.prepare("INSERT INTO shortcuts (`name`, `command`) VALUES(?, ?)");
				stmt.setString(1, params[2]);
				stmt.setString(2, params[3]);
				moo.db.executeUpdate();
				
				if (replaced)
					moo.reply(source, target, "Shortcut changed");
				else
					moo.reply(source, target, "Shortcut added");
			}
			catch (SQLException ex)
			{
				logger.getGlobalLogger().log(ex);
			}
		}
		else if (params.length > 2 && params[1].equalsIgnoreCase("del"))
		{
			try
			{
				PreparedStatement stmt = moo.db.prepare("SELECT command FROM shortcuts WHERE `name` = ?");
				stmt.setString(1, params[2]);
				ResultSet rs = moo.db.executeQuery();
				if (!rs.next())
				{
					moo.reply(source, target, "This shortcut does not exist");
					return;
				}
				
				stmt = moo.db.prepare("DELETE FROM shortcuts WHERE `name` = ?");
				stmt.setString(1, params[2]);
				moo.db.executeUpdate();
				
				moo.reply(source, target, "Shortcut deleted");
			}
			catch (SQLException ex)
			{
				logger.getGlobalLogger().log(ex);
			}
		}
		else if (params.length > 2)
		{
			protocol proto = protocol.findProtocol("ssh");
			if (proto == null)
			{
				moo.reply(source, target, "No such protocol SSH?");
				return;
			}
			
			serverInfo[] server_info = servercontrol.findServers(params[2], proto.getProtocolName());
			if (server_info == null)
			{
				moo.reply(source, target, "No servers found for " + params[2] + " using " + proto.getProtocolName());
				return;
			}
			
			String command = null;
			try
			{
				PreparedStatement stmt = moo.db.prepare("SELECT command FROM shortcuts WHERE `name` = ?");
				stmt.setString(1, params[1]);
				ResultSet rs = moo.db.executeQuery();
				if (rs.next())
					command = rs.getString("command");
			}
			catch (SQLException ex)
			{
				logger.getGlobalLogger().log(ex);
				return;
			}
			
			if (command == null)
			{
				moo.reply(source, target, "No such shortcut " + params[1]);
				return;
			}
			
			for (serverInfo si : server_info)
			{
				try
				{
					connection con = connection.findOrCreateConncetion(si);
					process proc = new echoProcess(con, source, target, command);
					proc.start();
				}
				catch (Exception ex)
				{
					moo.reply(source, target, "Error executing command on " + si.host + ": " + ex.getMessage());
				}
			}
		}
		else
		{
			moo.reply(source, target, "Syntax:");
			moo.reply(source, target, "!s list");
			moo.reply(source, target, "!s add name command");
			moo.reply(source, target, "!s del name");
			moo.reply(source, target, "!s name server");
		}
	}
}