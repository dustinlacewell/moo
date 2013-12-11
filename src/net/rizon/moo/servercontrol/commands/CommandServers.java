package net.rizon.moo.servercontrol.commands;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.servercontrol.ServerInfo;
import net.rizon.moo.servercontrol.servercontrol;

public class CommandServers extends Command
{
	public CommandServers(Plugin pkg)
	{
		super(pkg, "!SERVERS", "View server list");
		this.requiresChannel(Moo.conf.getAdminChannels());
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length == 1)
		{
			ServerInfo[] servers = servercontrol.getServers();
			if (servers == null)
			{
				Moo.reply(source, target, "There are no configured servers.");
				return;
			}
			
			Moo.reply(source, target, "There are " + servers.length + " servers");
			for (ServerInfo si : servers)
				Moo.reply(source, target, si.name + ":" + si.protocol + ", user: " + si.user + ", group: " + si.group);
		}
		else
		{
			ServerInfo[] servers = servercontrol.findServers(params[1]);
			if (servers == null)
			{
				Moo.reply(source, target, "No servers match " + params[1]);
				return;
			}
			

			Moo.reply(source, target, "There are " + servers.length + " servers that match " + params[1]);
			for (ServerInfo si : servers)
				Moo.reply(source, target, si.name + ":" + si.protocol + ", user: " + si.user + ", group: " + si.group);
		}
	}
}