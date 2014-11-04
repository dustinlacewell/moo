package net.rizon.moo.plugin.core;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

import java.util.logging.Level;

class CommandPlugins extends Command
{
	public CommandPlugins(Plugin pkg)
	{
		super(pkg, "!PLUGINS", "Manage plugins");
		this.requiresChannel(Moo.conf.admin_channels);
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length <= 2)
		{
			source.reply("Currently loaded plugins:");
			
			for (Plugin pl : Plugin.getPlugins())
				source.reply("  " + pl.pname + " (" + pl.getName() + ")");
		}
		else if (params[1].equalsIgnoreCase("load"))
		{
			try
			{
				Plugin p = Plugin.loadPlugin(params[2]);
				source.reply("Plugin " + p.getName() + " loaded");
			}
			catch (Throwable ex)
			{
				source.reply("Unable to load plugin " + params[2] + ": " + ex.getMessage());
				Logger.getGlobalLogger().log(Level.WARNING, "Unable to load plugin " + params[2], ex);
			}
		}
		else if (params[1].equalsIgnoreCase("unload"))
		{
			Plugin p = Plugin.findPlugin(params[2]);
			if (p == null)
			{
				source.reply("Plugin " + params[2] + " is not loaded");
			}
			else
			{
				p.remove();
				source.reply("Plugin " + p.getName() + " removed");
			}
		}
		else if (params[1].equalsIgnoreCase("reload"))
		{
			Plugin p = Plugin.findPlugin(params[2]);
			if (p == null)
			{
				source.reply("Plugin " + params[2] + " is not loaded");
			}
			else
			{
				p.remove();

				try
				{
					p = Plugin.loadPlugin(params[2]);
					source.reply("Plugin " + p.getName() + " reloaded");
				}
				catch (Throwable ex)
				{
					source.reply("Unable to load plugin " + params[2] + ": " + ex.getMessage());
					Logger.getGlobalLogger().log(Level.WARNING, "Unable to reload plugin " + params[2], ex);
				}
			}
		}
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax:");
		source.notice("!PLUGINS -- lists currently loaded plugins");
		source.notice("!PLUGINS LOAD <name> -- loads a plugin");
		source.notice("!PLUGINS UNLOAD <name> -- unloads a plugin");
		source.notice("!PLUGINS RELOAD <name> -- reloads a plugin");
	}
}
