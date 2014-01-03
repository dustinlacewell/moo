package net.rizon.moo.servercontrol.commands;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.servercontrol.Connection;
import net.rizon.moo.servercontrol.EchoProcess;
import net.rizon.moo.servercontrol.Process;
import net.rizon.moo.servercontrol.Protocol;
import net.rizon.moo.servercontrol.ServerInfo;
import net.rizon.moo.servercontrol.servercontrol;

public class CommandServerControl extends Command
{
	public CommandServerControl(Plugin pkg)
	{
		super(pkg, "!EXEC", "Execute commands on remote servers");
		this.requiresChannel(Moo.conf.getList("admin_channels"));
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length < 4)
		{
			Moo.reply(source, target, "Syntax: !exec server protocol command");
			return;
		}
		
		Protocol proto = Protocol.findProtocol(params[2]);
		if (proto == null)
		{
			Moo.reply(source, target, "No such protocol " + params[2]);
			return;
		}
		
		ServerInfo[] server_info = servercontrol.findServers(params[1], params[2]);
		if (server_info == null)
		{
			Moo.reply(source, target, "No servers found for " + params[1] + " using " + params[2]);
			return;
		}
		
		String command = params[3];
		for (int i = 4; i < params.length; ++i)
			command += " " + params[i];
		
		for (ServerInfo si : server_info)
		{
			Connection con = Connection.findOrCreateConncetion(si);
			Process proc = new EchoProcess(con, source, target, command);
			proc.start();
		}
	}
}
