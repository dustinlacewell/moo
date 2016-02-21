package net.rizon.moo.plugin.commands.map;

import com.google.inject.Inject;
import java.util.Iterator;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;
import net.rizon.moo.util.Match;

abstract class CommandMap extends Command
{
	@Inject
	private ServerManager serverManager;

	@Inject
	private Protocol protocol;

	private boolean full;

	public CommandMap(Config conf, String cmd, boolean full)
	{
		super(cmd, "View hub lag and routing information");
		this.full = full;

		this.requiresChannel(conf.staff_channels);
		this.requiresChannel(conf.oper_channels);
		this.requiresChannel(conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: " + this.getCommandName() + " [{ usercount | HUB server.name | FIND mask}]");
		source.notice("Searches for information about servers.");
		source.notice("Without any further arguments, the sendq (in bytes) of hubs is shown.");
		if(!this.getCommandName().equalsIgnoreCase("!MAP-"))
			source.notice("The sendq output will be hidden unless it exceeds 1023 bytes, use !MAP- to see them.");
		source.notice("If a user count is given, only servers and their user count with that amount of");
		source.notice("(or more) users will be shown.");
		source.notice("HUB server.name shows what other servers server.name is connected to.");
		source.notice("FIND mask tries to find all servers matching the given mask.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length == 1)
		{
			for (Server s : serverManager.getServers())
				if (s.isHub())
				{
					s.bytes = 0;
					protocol.write("STATS", "?", s.getName());
				}
			Message219.request_all = this.full;
			Message219.source = source;
		}
		else if (params.length > 1)
		{
			if (params[1].equalsIgnoreCase("HUB") && params.length > 2)
			{
				Server s = serverManager.findServer(params[2]);
				if (s == null)
					source.reply("[MAP] Server " + params[2] + " not found");
				else
					for (Iterator<Server> it = s.links.iterator(); it.hasNext();)
						source.reply("[MAP] " + s.getName() + " is linked to " + it.next().getName());
			}
			else if (params[1].equalsIgnoreCase("FIND") && params.length > 2)
			{
				int count = 0;
				for (Server s : serverManager.getServers())
				{
					if (Match.matches(s.getName(), "*" + params[2] + "*"))
					{
						source.reply("[MAP] Server " + s.getName() + " matches " + params[2]);
						++count;
					}
				}
				source.reply("[MAP] End of match, " + count + " servers found");
			}
			else
			{
				try
				{
					int users = Integer.parseInt(params[1]);

					for (Server s : serverManager.getServers())
						protocol.write("USERS", s.getName());

					Message265.source = source;
					Message265.request_users = users;
				}
				catch (NumberFormatException ex)
				{
					Server s = serverManager.findServer(params[1]);
					if (s == null)
						source.reply("[MAP] Server " + params[1] + " not found");
					else
					{
						s.bytes = 0;
						protocol.write("STATS", "?", s.getName());;
						Message219.request_all = this.full;
						Message219.source = source;
					}
				}
			}
		}
	}
}
