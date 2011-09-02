package net.rizon.moo.commands;

import net.rizon.moo.command;
import net.rizon.moo.moo;


public class commandShutdown extends command
{
	public commandShutdown()
	{
		super("!SHUTDOWN");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (moo.conf.isAdminChannel(target) == false)
			return;

		moo.sock.privmsg(target, "Shutting down");
		moo.sock.write("QUIT :SHUTDOWN from " + source);
		moo.sock.shutdown();
		moo.quitting = true;
	}
}
