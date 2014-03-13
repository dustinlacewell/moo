package net.rizon.moo.plugin.servercontrol.commands;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.servercontrol.Connection;

public class CommandConnections extends Command
{
	public CommandConnections(Plugin pkg)
	{
		super(pkg, "!CONNECTIONS", "View active connections");
		this.requiresChannel(Moo.conf.getList("admin_channels"));
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