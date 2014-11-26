package net.rizon.moo.plugin.core;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;


class CommandShutdown extends Command
{
	public CommandShutdown(Plugin pkg)
	{
		super(pkg, "!SHUTDOWN", "Shutdown " + Moo.conf.general.nick);
		this.requiresChannel(Moo.conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !SHUTDOWN");
		source.notice("!SHUTDOWN shuts " + Moo.conf.general.nick + " down.");
		source.notice("Please note that this will show the nick!user@host of the user");
		source.notice("issuing this command in the quit message.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		source.reply("Shutting down");
		Moo.sock.write("QUIT :SHUTDOWN from " + source.getUser().getName());
		Moo.sock.shutdown();
		Moo.quitting = true;
	}
}
