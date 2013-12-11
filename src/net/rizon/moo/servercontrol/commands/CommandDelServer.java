package net.rizon.moo.servercontrol.commands;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.rizon.moo.Command;
import net.rizon.moo.Database;
import net.rizon.moo.Moo;
import net.rizon.moo.MPackage;

public class CommandDelServer extends Command
{
	public CommandDelServer(MPackage pkg)
	{
		super(pkg, "!DELSERVER", "Deletes a server");
		this.requiresChannel(Moo.conf.getAdminChannels());
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length < 3)
		{
			Moo.reply(source, target, "Syntax: !delserver host protocol");
			return;
		}
		
		try
		{
			PreparedStatement statement = Moo.db.prepare("DELETE FROM servercontrol WHERE `name` = ? AND `protocol` = ?");
			statement.setString(1, params[1]);
			statement.setString(2, params[2]);
			Moo.db.executeUpdate();
			Moo.reply(source, target, "Done.");
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
		}
	}
}
