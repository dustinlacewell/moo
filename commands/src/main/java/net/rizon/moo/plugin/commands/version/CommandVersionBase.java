package net.rizon.moo.plugin.commands.version;

import com.google.inject.Inject;
import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;
import org.slf4j.Logger;

abstract class CommandVersionBase extends Command
{
	@Inject
	protected static Logger logger;

	@Inject
	private ServerManager serverManager;

	@Inject
	private Protocol protocol;

	static Server want_server = null;

	public static boolean onlyOld;

	public CommandVersionBase(Config conf, String command)
	{
		super(command, "View the IRCd versions");

		this.requiresChannel(conf.oper_channels);
		this.requiresChannel(conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !VERSIONS [OLD|server]");
		source.notice("This command gets the version and serno of all currently linked IRCds and lists them.");
		source.notice("If OLD is given as a parameter, only versions that aren't the latest will be shown.");
		source.notice("If a server name is given, the version for that server will be shown.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length > 1)
		{
			if (params[1].equalsIgnoreCase("OLD"))
			{
				onlyOld = true;
				want_server = null;
			}
			else
			{
				onlyOld = false;
				want_server = serverManager.findServer(params[1]);
			}
		}
		else
			want_server = null;

		for (Server s : serverManager.getServers())
		{
			if (s.isServices() == false)
			{
				protocol.write("VERSION", s.getName());
				Message351.waiting_for.add(s.getName());
			}
		}

		Message351.command_source = source;
	}
}