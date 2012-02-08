package net.rizon.moo.servercontrol.commands;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.servercontrol.connection;

public class commandConnections extends command
{
	public commandConnections(mpackage pkg)
	{
		super(pkg, "!CONNECTIONS", "View active connections");
		this.requireAdmin();
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		connection[] cons = connection.getConnections();
		moo.reply(source, target, "There are currently " + cons.length + " active connections");
		for (int i = 0; i < cons.length; ++i)
		{
			connection con = cons[i];
			moo.reply(source, target, (i + 1) + ": " + con.getServerInfo().name + ":" + con.getServerInfo().port + " " + con.getProtocol().getProtocolName());
		}
	}
}