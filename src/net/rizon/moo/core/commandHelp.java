package net.rizon.moo.core;

import java.util.Arrays;
import java.util.Iterator;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

class commandHelpBase extends command
{
	public commandHelpBase(mpackage pkg, final String command)
	{
		super(pkg, command, "Shows this list");
	}
	
	@Override
	public void onHelp(String source)
	{
		moo.notice(source, "Syntax: " + this.getCommandName() + " [command]");
		moo.notice(source, this.getCommandName() + " lists all available commands or gives more verbose information on a command.");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		for (Iterator<mpackage> it = mpackage.getPackages().iterator(); it.hasNext();)
		{
			mpackage pkg = it.next();
			boolean show_header = false;
			
			for (Iterator<command> it2 = pkg.getCommands().iterator(); it2.hasNext();)
			{
				command c = it2.next();
				
				if (c.getRequiredChannels() != null && Arrays.asList(c.getRequiredChannels()).contains(target) == false)
					continue;
				
				if (params.length == 1)
				{
					if (show_header == false)
					{
						moo.notice(source, pkg.getPackageName() + " - " + pkg.getDescription());
						show_header = true;
					}
					
					c.onHelpList(source);
				}
				else if ((c.getCommandName().length() > 1 && c.getCommandName().substring(1).equalsIgnoreCase(params[1]))
						|| c.getCommandName().equalsIgnoreCase(params[1]))
				{
					c.onHelp(source);
					return;
				}
			}
		}
	}
}

class commandHelp
{
	public commandHelp(mpackage pkg)
	{
		new commandHelpBase(pkg, "!MOO-HELP");
		new commandHelpBase(pkg, "!HELP");
	}
}