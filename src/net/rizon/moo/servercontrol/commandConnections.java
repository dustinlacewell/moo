package net.rizon.moo.servercontrol;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

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
		moo.sock.reply(source, target, "There are currently " + cons.length + " active connections");
		for (int i = 0; i < cons.length; ++i)
		{
			connection con = cons[i];
			moo.sock.reply(source, target, (i + 1) + ": " + con.getHost() + ":" + con.getPort() + " " + con.getProtocol().getProtocolName());
		}
	}
}