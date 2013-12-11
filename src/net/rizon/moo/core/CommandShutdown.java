package net.rizon.moo.core;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;


class CommandShutdown extends Command
{
	public CommandShutdown(Plugin pkg)
	{
		super(pkg, "!SHUTDOWN", "Shutdown " + Moo.conf.getNick());
		this.requiresChannel(Moo.conf.getAdminChannels());
	}

	@Override
	public void onHelp(String source)
	{
		Moo.notice(source, "Syntax: !SHUTDOWN");
		Moo.notice(source, "!SHUTDOWN shuts " + Moo.conf.getNick() + " down.");
		Moo.notice(source, "Please note that this will show the nick!user@host of the user");
		Moo.notice(source, "issuing this command in the quit message.");
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		Moo.reply(source, target, "Shutting down");
		Moo.sock.write("QUIT :SHUTDOWN from " + source);
		Moo.sock.shutdown();
		Moo.quitting = true;
	}
}