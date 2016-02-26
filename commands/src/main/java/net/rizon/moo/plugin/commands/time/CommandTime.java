package net.rizon.moo.plugin.commands.time;

import com.google.inject.Inject;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;

public class CommandTime extends Command
{
	@Inject
	private ServerManager serverManager;

	@Inject
	private Protocol protocol;

	@Inject
	public CommandTime(Config conf)
	{
		super("!TIME", "View server times");

		this.requiresChannel(conf.oper_channels);
		this.requiresChannel(conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !TIME");
		source.notice("Queries all IRCds about their current time and returns the responses.");
		source.notice("If there are significant differences in time between servers (at least");
		source.notice("60 seconds), the offset will be shown");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		for (Server s : serverManager.getServers())
		{
			if (s.isServices())
				continue;
			
			protocol.write("TIME", s.getName());
			Message391.waiting_for.add(s.getName());
		}

		Message391.known_times.clear();
		Message391.hourly_check = false;
		Message391.command_source = source;
	}
}