package net.rizon.moo.commands;

import java.util.Iterator;

import net.rizon.moo.command;
import net.rizon.moo.moo;

public class commandHelp extends command
{
	public commandHelp()
	{
		super("!MOO-HELP", "Shows this list");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		moo.sock.reply(source, target, "Available commands:");
		for (Iterator<command> it = command.getCommands().iterator(); it.hasNext();)
		{
			command c = it.next();
			
			if (c.requiresAdmin() && moo.conf.isAdminChannel(target) == false)
				continue;
			
			c.onHelp(source, target);
		}
	}
}