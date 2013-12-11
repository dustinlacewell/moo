package net.rizon.moo.servercontrol.commands;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.MPackage;
import net.rizon.moo.servercontrol.Connection;

public class CommandConnections extends Command
{
	public CommandConnections(MPackage pkg)
	{
		super(pkg, "!CONNECTIONS", "View active connections");
		this.requiresChannel(Moo.conf.getAdminChannels());
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		Connection[] cons = Connection.getConnections();
		Moo.reply(source, target, "There are currently " + cons.length + " active connections");
		for (int i = 0; i < cons.length; ++i)
		{
			Connection con = cons[i];
			Moo.reply(source, target, (i + 1) + ": " + con.getServerInfo().name + ":" + con.getServerInfo().port + " " + con.getProtocol().getProtocolName());
		}
	}
}