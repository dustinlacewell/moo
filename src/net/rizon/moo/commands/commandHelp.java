package net.rizon.moo.commands;

import java.util.Iterator;

import net.rizon.moo.command;
import net.rizon.moo.moo;

public class commandHelp extends command
{
	public commandHelp()
	{
		super("!MOO-HELP");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		moo.sock.notice(source, "Available commands:");
		for (Iterator<command> it = command.getCommands().iterator(); it.hasNext();)
		{
			command c = it.next();
			
			if (c.requiresAdmin() && moo.conf.isAdminChannel(target) == false)
				continue;
			
			moo.sock.notice(source, c.getCommandName());
		}
	}
}