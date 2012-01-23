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
		if (params.length < 6)
		{
			moo.sock.reply(source, target, "Syntax: !addserver host port protocol user pass group");
			return;
		}
		
		int port;
		try
		{
			port = Integer.parseInt(params[2]);
		}
		catch (NumberFormatException ex)
		{
			moo.sock.reply(source, target, "Invalid port");
			return;
		}
		
		if (protocol.findProtocol(params[3]) == null)
		{
			moo.sock.reply(source, target, "Unknown protocol " + params[3]);
			return;
		}
		
		try
		{
			PreparedStatement statement = moo.db.prepare("INSERT INTO servercontrol (`host`, `port`, `protocol`, `user`, `pass`, `group`) VALUES (?, ?, ?, ?, ?, ?)");
			statement.setString(1, params[1]);
			statement.setInt(2, port);
			statement.setString(3, params[3]);
			statement.setString(4, params[4]);
			statement.setString(5, params[5]);
			if (params.length > 6)
				statement.setString(6, params[6]);
			else
				statement.setString(6, "");
			moo.db.executeUpdate();
			
			moo.sock.reply(source, target, "Server added");
		}
		catch (SQLException ex)
		{
			database.handleException(ex);
		}
	}
}
