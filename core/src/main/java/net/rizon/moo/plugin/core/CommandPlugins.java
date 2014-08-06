package net.rizon.moo.plugin.core;

import java.util.logging.Level;

import net.rizon.moo.Command;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

class CommandPlugins extends Command
{
	public CommandPlugins(Plugin pkg)
	{
		super(pkg, "!PLUGINS", "Manage plugnis");
		this.requiresChannel(Moo.conf.getList("admin_channels"));
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length <= 2)
		{
			Moo.reply(source, target, "Currently loaded plugins:");
			
			for (Plugin pl : Plugin.getPlugins())
				Moo.reply(source, target, "  " + pl.pname + " (" + pl.getName() + ")");
		}
		else if (params[1].equalsIgnoreCase("load"))
		{
			try
			{
				Plugin p = Plugin.loadPlugin(params[2]);
				Moo.reply(source, target, "Plugin " + p.getName() + " loaded");
			}
			catch (Throwable ex)
			{
				Moo.reply(source, target, "Unable to load plugin " + params[2] + ": " + ex.getMessage());
				Logger.getGlobalLogger().log(Level.WARNING, "Unable to load plugin " + params[2], ex);
			}
		}
		else if (params[1].equalsIgnoreCase("unload"))
		{
			Plugin p = Plugin.findPlugin(params[2]);
			if (p == null)
			{
				Moo.reply(source, target, "Plugin " + params[2] + " is not loaded");
			}
			else
			{
				p.remove();
				Moo.reply(source, target, "Plugin " + p.getName() + " removed");
			}
		}
		else if (params[1].equalsIgnoreCase("reload"))
		{
			Plugin p = Plugin.findPlugin(params[2]);
			if (p == null)
			{
				Moo.reply(source, target, "Plugin " + params[2] + " is not loaded");
			}
			else
			{
				p.remove();

				try
				{
					p = Plugin.loadPlugin(params[2]);
					Moo.reply(source, target, "Plugin " + p.getName() + " reloaded");
				}
				catch (Throwable ex)
				{
					Moo.reply(source, target, "Unable to load plugin " + params[2] + ": " + ex.getMessage());
					Logger.getGlobalLogger().log(Level.WARNING, "Unable to reload plugin " + params[2], ex);
				}
			}
		}
	}
}
