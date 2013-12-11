package net.rizon.moo.servercontrol.commands;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.rizon.moo.Command;
import net.rizon.moo.Database;
import net.rizon.moo.Moo;
import net.rizon.moo.MPackage;
import net.rizon.moo.servercontrol.Protocol;

public class CommandAddServer extends Command
{
	public CommandAddServer(MPackage pkg)
	{
		super(pkg, "!ADDSERVER", "Add a server");
		this.requiresChannel(Moo.conf.getAdminChannels());
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length < 7)
		{
			Moo.reply(source, target, "Syntax: !addserver name host port protocol user pass group");
			return;
		}
		
		int port;
		try
		{
			port = Integer.parseInt(params[3]);
		}
		catch (NumberFormatException ex)
		{
			Moo.reply(source, target, "Invalid port");
			return;
		}
		
		if (Protocol.findProtocol(params[4]) == null)
		{
			Moo.reply(source, target, "Unknown protocol " + params[3]);
			return;
		}
		
		try
		{
			PreparedStatement statement = Moo.db.prepare("INSERT INTO servercontrol (`name`, `host`, `port`, `protocol`, `user`, `pass`, `group`) VALUES (?, ?, ?, ?, ?, ?, ?)");
			statement.setString(1, params[1]);
			statement.setString(2, params[2]);
			statement.setInt(3, port);
			statement.setString(4, params[4]);
			statement.setString(5, params[5]);
			statement.setString(6, params[6]);
			if (params.length > 7)
				statement.setString(7, params[7]);
			else
				statement.setString(7, "");
			Moo.db.executeUpdate();
			
			Moo.reply(source, target, "Server added");
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
		}
	}
}
