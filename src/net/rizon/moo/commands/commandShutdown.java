package net.rizon.moo.commands;

import net.rizon.moo.command;
import net.rizon.moo.moo;


public class commandShutdown extends command
{
	public commandShutdown()
	{
		super("!SHUTDOWN", "Shutdown " + moo.conf.getNick());
		this.requireAdmin();
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		moo.sock.privmsg(target, "Shutting down");
		moo.sock.write("QUIT :SHUTDOWN from " + source);
		moo.sock.shutdown();
		moo.quitting = true;
	}
}
