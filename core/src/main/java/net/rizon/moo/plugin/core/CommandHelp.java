package net.rizon.moo.plugin.core;

import com.google.inject.Inject;
import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Plugin;
import net.rizon.moo.PluginManager;

class CommandHelp extends Command
{
	@Inject
	private PluginManager pluginManager;

	CommandHelp()
	{
		super("!HELP", "Shows this list");
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: " + this.getCommandName() + " [command]");
		source.notice(this.getCommandName() + " lists all available commands or gives more verbose information on a command.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		for (Plugin pkg : pluginManager.getPlugins())
		{
			boolean show_header = false;

			for (Command c : pkg.getCommands())
			{
				if (!c.isRequiredChannel(source.getTargetName()))
					continue;

				if (params.length == 1)
				{
					if (show_header == false)
					{
						source.notice(pkg.getName() + " - " + pkg.getDescription());
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