package net.rizon.moo.servercontrol.commands;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.rizon.moo.command;
import net.rizon.moo.database;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.servercontrol.protocol;

public class commandAddServer extends command
{
	public commandAddServer(mpackage pkg)
	{
		super(pkg, "!ADDSERVER", "Add a server");
		this.requireAdmin();
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length < 7)
		{
			moo.sock.reply(source, target, "Syntax: !addserver name host port protocol user pass group");
			return;
		}
		
		int port;
		try
		{
			port = Integer.parseInt(params[3]);
		}
		catch (NumberFormatException ex)
		{
			moo.sock.reply(source, target, "Invalid port");
			return;
		}
		
		if (protocol.findProtocol(params[4]) == null)
		{
			moo.sock.reply(source, target, "Unknown protocol " + params[3]);
			return;
		}
		
		try
		{
			PreparedStatement statement = moo.db.prepare("INSERT INTO servercontrol (`name`, `host`, `port`, `protocol`, `user`, `pass`, `group`) VALUES (?, ?, ?, ?, ?, ?, ?)");
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
			moo.db.executeUpdate();
			
			moo.sock.reply(source, target, "Server added");
		}
		catch (SQLException ex)
		{
			database.handleException(ex);
		}
	}
}
