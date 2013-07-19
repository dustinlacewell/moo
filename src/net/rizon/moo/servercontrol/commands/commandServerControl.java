package net.rizon.moo.servercontrol.commands;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.servercontrol.connection;
import net.rizon.moo.servercontrol.echoProcess;
import net.rizon.moo.servercontrol.process;
import net.rizon.moo.servercontrol.protocol;
import net.rizon.moo.servercontrol.serverInfo;
import net.rizon.moo.servercontrol.servercontrol;

public class commandServerControl extends command
{
	public commandServerControl(mpackage pkg)
	{
		super(pkg, "!EXEC", "Execute commands on remote servers");
		this.requiresChannel(moo.conf.getAdminChannels());
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length < 4)
		{
			moo.reply(source, target, "Syntax: !exec server protocol command");
			return;
		}
		
		protocol proto = protocol.findProtocol(params[2]);
		if (proto == null)
		{
			moo.reply(source, target, "No such protocol " + params[2]);
			return;
		}
		
		serverInfo[] server_info = servercontrol.findServers(params[1], params[2]);
		if (server_info == null)
		{
			moo.reply(source, target, "No servers found for " + params[1] + " using " + params[2]);
			return;
		}
		
		String command = params[3];
		for (int i = 4; i < params.length; ++i)
			command += " " + params[i];
		
		for (serverInfo si : server_info)
		{
			connection con = connection.findOrCreateConncetion(si);
			process proc = new echoProcess(con, source, target, command);
			proc.start();
		}
	}
}
