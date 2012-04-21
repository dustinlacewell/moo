package net.rizon.moo.core;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;


public class commandShutdown extends command
{
	public commandShutdown(mpackage pkg)
	{
		super(pkg, "!SHUTDOWN", "Shutdown " + moo.conf.getNick());
		this.requireAdmin();
	}

	@Override
	public void onHelp(String source)
	{
		moo.notice(source, "Syntax: !SHUTDOWN");
		moo.notice(source, "!SHUTDOWN shuts " + moo.conf.getNick() + " down.");
		moo.notice(source, "Please note that this will show the nick!user@host of the user");
		moo.notice(source, "issuing this command in the quit message.");
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		moo.reply(source, target, "Shutting down");
		moo.sock.write("QUIT :SHUTDOWN from " + source);
		moo.sock.shutdown();
		moo.quitting = true;
	}
}
