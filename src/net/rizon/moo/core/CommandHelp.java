package net.rizon.moo.core;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

class commandHelpBase extends Command
{
	public commandHelpBase(Plugin pkg, final String command)
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
		for (Plugin pkg : Plugin.getPlugins())
		{
			boolean show_header = false;
			
			for (Command c : pkg.commands)
			{
				if (!c.isRequiredChannel(target))
					continue;
				
				if (params.length == 1)
				{
					if (show_header == false)
					{
						Moo.notice(source, pkg.getName() + " - " + pkg.getDescription());
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
	private Command mo, h;
	
	public CommandHelp(Plugin pkg)
	{
		mo = new commandHelpBase(pkg, "!MOO-HELP");
		h = new commandHelpBase(pkg, "!HELP");
	}
	
	public void remove()
	{
		mo.remove();
		h.remove();
	}
}