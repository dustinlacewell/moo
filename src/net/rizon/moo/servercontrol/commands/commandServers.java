package net.rizon.moo.servercontrol.commands;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.servercontrol.serverInfo;
import net.rizon.moo.servercontrol.servercontrol;

public class commandServers extends command
{
	public commandServers(mpackage pkg)
	{
		super(pkg, "!SERVERS", "View server list");
		this.requireAdmin();
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length == 1)
		{
			serverInfo[] servers = servercontrol.getServers();
			if (servers == null)
			{
				moo.reply(source, target, "There are no configured servers.");
				return;
			}
			
			moo.reply(source, target, "There are " + servers.length + " servers");
			for (serverInfo si : servers)
				moo.reply(source, target, si.name + ":" + si.protocol + ", user: " + si.user + ", group: " + si.group);
		}
		else
		{
			serverInfo[] servers = servercontrol.findServers(params[1]);
			if (servers == null)
			{
				moo.reply(source, target, "No servers match " + params[1]);
				return;
			}
			

			moo.reply(source, target, "There are " + servers.length + " servers that match " + params[1]);
			for (serverInfo si : servers)
				moo.reply(source, target, si.name + ":" + si.protocol + ", user: " + si.user + ", group: " + si.group);
		}
	}
}