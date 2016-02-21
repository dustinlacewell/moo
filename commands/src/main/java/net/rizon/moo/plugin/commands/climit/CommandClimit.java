package net.rizon.moo.plugin.commands.climit;

import com.google.inject.Inject;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;

public class CommandClimit extends Command
{
	@Inject
	private ServerManager serverManager;

	@Inject
	private Protocol protocol;
	
	@Inject
	public CommandClimit(Config conf)
	{
		super("!CLIMIT", "View server channel limits");
		this.requiresChannel(conf.staff_channels);
		this.requiresChannel(conf.oper_channels);
		this.requiresChannel(conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !CLIMIT");
		source.notice("Shows how many channels clients may join for all servers.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		for (Server s : serverManager.getServers())
		{
			if (!s.isNormal())
				continue;

			protocol.write("VERSION", s.getName());
			MessageLimit.waiting_for.add(s.getName());
		}

		MessageLimit.source = source;
	}
}