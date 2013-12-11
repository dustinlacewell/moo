package net.rizon.moo.servercontrol.commands;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.rizon.moo.Command;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.servercontrol.Connection;
import net.rizon.moo.servercontrol.EchoProcess;
import net.rizon.moo.servercontrol.Process;
import net.rizon.moo.servercontrol.Protocol;
import net.rizon.moo.servercontrol.ServerInfo;
import net.rizon.moo.servercontrol.servercontrol;
import net.rizon.moo.servercontrol.UploadProcess;

public class CommandShortcut extends Command
{
	public CommandShortcut(Plugin pkg)
	{
		super(pkg, "!S", "Configure and execute shortcuts");
		this.requiresChannel(Moo.conf.getAdminChannels());
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length > 1 && params[1].equalsIgnoreCase("list"))
		{
			try
			{
				Moo.db.prepare("SELECT name FROM shortcuts");
				ResultSet rs = Moo.db.executeQuery();
				
				String shortcuts = "";
				while (rs.next())
					shortcuts += ", " + rs.getString("name");
				
				if (shortcuts.isEmpty())
					Moo.reply(source, target, "There are no configured shortcuts");
				else
					Moo.reply(source, target, "Shortcuts: " + shortcuts.substring(2));
			}
			catch (SQLException ex)
			{
				Logger.getGlobalLogger().log(ex);
			}
		}
		else if (params.length > 3 && (params[1].equalsIgnoreCase("addc") || params[1].equalsIgnoreCase("addf")))
		{
			try
			{
				boolean isFile = params[1].equalsIgnoreCase("addf");
				PreparedStatement stmt = Moo.db.prepare("DELETE FROM shortcuts WHERE `name` = ?");
				stmt.setString(1, params[2]);
				boolean replaced = Moo.db.executeUpdate() != 0;
				
				stmt = Moo.db.prepare("INSERT INTO shortcuts (`name`, `" + (isFile ? "file" : "command") + "`) VALUES(?, ?)");
				stmt.setString(1, params[2]);
				stmt.setString(2, params[3]);
				Moo.db.executeUpdate();
				
				if (replaced)
					Moo.reply(source, target, "Shortcut changed");
				else
					Moo.reply(source, target, "Shortcut added");
			}
			catch (SQLException ex)
			{
				Logger.getGlobalLogger().log(ex);
			}
		}
		else if (params.length > 2 && params[1].equalsIgnoreCase("del"))
		{
			try
			{
				PreparedStatement stmt = Moo.db.prepare("SELECT command FROM shortcuts WHERE `name` = ?");
				stmt.setString(1, params[2]);
				ResultSet rs = Moo.db.executeQuery();
				if (!rs.next())
				{
					Moo.reply(source, target, "This shortcut does not exist");
					return;
				}
				
				stmt = Moo.db.prepare("DELETE FROM shortcuts WHERE `name` = ?");
				stmt.setString(1, params[2]);
				Moo.db.executeUpdate();
				
				Moo.reply(source, target, "Shortcut deleted");
			}
			catch (SQLException ex)
			{
				Logger.getGlobalLogger().log(ex);
			}
		}
		else if (params.length > 2)
		{
			String sname = params[1], sproto = params[2], args = "";
			for (int i = 3; i < params.length; ++i)
				args += params[i] + " ";
			args = args.trim();
			
			Protocol proto = Protocol.findProtocol("ssh");
			if (proto == null)
			{
				Moo.reply(source, target, "No such protocol SSH?");
				return;
			}
			
			ServerInfo[] server_info = servercontrol.findServers(sproto, proto.getProtocolName());
			if (server_info == null)
			{
				Moo.reply(source, target, "No servers found for " + sproto + " using " + proto.getProtocolName());
				return;
			}
			
			String command = null, file = null;
			try
			{
				PreparedStatement stmt = Moo.db.prepare("SELECT command,file FROM shortcuts WHERE `name` = ?");
				stmt.setString(1, sname);
				ResultSet rs = Moo.db.executeQuery();
				if (rs.next())
				{
					command = rs.getString("command");
					file = rs.getString("file");
				}
			}
			catch (SQLException ex)
			{
				Logger.getGlobalLogger().log(ex);
				return;
			}
			
			if (command == null && file == null)
			{
				Moo.reply(source, target, "No such shortcut " + sname);
				return;
			}
			
			File f = new File(Moo.conf.getShortcutBase(), file);
			if (!f.exists())
			{
				Moo.reply(source, target, "File for shortcut " + sname + " (" + file + ") does not exist?");
				return;
			}
			
			for (ServerInfo si : server_info)
			{
				try
				{
					Connection con = Connection.findOrCreateConncetion(si);

					if (command != null)
					{
						Process proc = new EchoProcess(con, source, target, command);
						proc.start();
					}
					if (file != null)
					{
						Process proc = new UploadProcess(con, f, source, target, args);
						proc.start();
					}
				}
				catch (Exception ex)
				{
					Moo.reply(source, target, "Error executing command on " + si.host + ": " + ex.getMessage());
				}
			}
		}
		else
		{
			Moo.reply(source, target, "Syntax:");
			Moo.reply(source, target, "!s list");
			Moo.reply(source, target, "!s addc name command");
			Moo.reply(source, target, "!s addf name file");
			Moo.reply(source, target, "!s del name");
			Moo.reply(source, target, "!s name server args...");
		}
	}
}