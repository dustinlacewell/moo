package net.rizon.moo.servercontrol.commands;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.rizon.moo.command;
import net.rizon.moo.database;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

public class commandDelServer extends command
{
	public commandDelServer(mpackage pkg)
	{
		super(pkg, "!DELSERVER", "Deletes a server");
		this.requireAdmin();
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length < 3)
		{
			moo.sock.reply(source, target, "Syntax: !delserver host protocol");
			return;
		}
		
		try
		{
			PreparedStatement statement = moo.db.prepare("DELETE FROM servercontrol WHERE `name` = ? AND `protocol` = ?");
			statement.setString(1, params[1]);
			statement.setString(2, params[2]);
			moo.db.executeUpdate();
			moo.sock.reply(source, target, "Done.");
		}
		catch (SQLException ex)
		{
			database.handleException(ex);
		}
	}
}
