package net.rizon.moo.plugin.commands.slackers;

import com.google.inject.Inject;
import java.util.HashSet;
import java.util.LinkedList;

import java.util.List;
import java.util.Set;
import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;

public class CommandSlackers extends Command
{
	@Inject
	private ServerManager serverManager;

	@Inject
	private Protocol protocol;
	
	public static List<String> opers = new LinkedList<>();
	public static CommandSource command_source;
	public static Set<String> waiting_on = new HashSet<>();

	@Inject
	public CommandSlackers(Config conf)
	{
		super("!SLACKERS", "Find opers online but not in the channel");

		this.requiresChannel(conf.staff_channels);
		this.requiresChannel(conf.oper_channels);
		this.requiresChannel(conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !SLACKERS");
		source.notice("Searches for all online opers who are not in this channel.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		opers.clear();
		waiting_on.clear();
		for (Server s : serverManager.getServers())
			if (s.isNormal())
			{
				protocol.write("STATS", "p", s.getName());
				waiting_on.add(s.getName());
			}
		command_source = source;
	}
}