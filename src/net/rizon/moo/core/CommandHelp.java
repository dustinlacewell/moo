package net.rizon.moo.core;

import java.util.Arrays;
import java.util.Iterator;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.MPackage;

class commandHelpBase extends Command
{
	public commandHelpBase(MPackage pkg, final String command)
	{
		super(pkg, command, "Shows this list");
	}
	
	@Override
	public void onHelp(String source)
	{
		Moo.notice(source, "Syntax: " + this.getCommandName() + " [command]");
		Moo.notice(source, this.getCommandName() + " lists all available commands or gives more verbose information on a command.");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		for (Iterator<MPackage> it = MPackage.getPackages().iterator(); it.hasNext();)
		{
			MPackage pkg = it.next();
			boolean show_header = false;
			
			for (Iterator<Command> it2 = pkg.getCommands().iterator(); it2.hasNext();)
			{
				Command c = it2.next();
				
				if (c.getRequiredChannels() != null && Arrays.asList(c.getRequiredChannels()).contains(target) == false)
					continue;
				
				if (params.length == 1)
				{
					if (show_header == false)
					{
						Moo.notice(source, pkg.getPackageName() + " - " + pkg.getDescription());
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

class CommandHelp
{
	public CommandHelp(MPackage pkg)
	{
		new commandHelpBase(pkg, "!MOO-HELP");
		new commandHelpBase(pkg, "!HELP");
	}
}