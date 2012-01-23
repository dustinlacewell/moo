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
	public void execute(String source, String target, String[] params)
	{
		moo.sock.reply(source, target, "Shutting down");
		moo.sock.write("QUIT :SHUTDOWN from " + source);
		moo.sock.shutdown();
		moo.quitting = true;
	}
}
