package net.rizon.moo.plugin.core;

import com.google.inject.Inject;
import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;


class CommandShutdown extends Command
{
	@Inject
	private Protocol protocol;

	@Inject
	private Config conf;

	@Inject
	CommandShutdown(Config conf)
	{
		super("!SHUTDOWN", "Shutdown " + conf.general.nick);
		this.requiresChannel(conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !SHUTDOWN");
		source.notice("!SHUTDOWN shuts " + conf.general.nick + " down.");
		source.notice("Please note that this will show the nick!user@host of the user");
		source.notice("issuing this command in the quit message.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		source.reply("Shutting down");
		protocol.write("QUIT", "SHUTDOWN from " + source.getUser().getNick());
		Moo.stop();
		Moo.quitting = true;
	}
}
