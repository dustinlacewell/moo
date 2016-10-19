package net.rizon.moo.plugin.core;


import com.google.inject.Inject;
import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.PluginManager;
import net.rizon.moo.conf.Config;
import org.eclipse.aether.artifact.Artifact;
import org.slf4j.Logger;

class CommandPlugins extends Command
{
	@Inject
	private static Logger logger;

	@Inject
	private Moo moo;

	@Inject
	private PluginManager pluginManager;
	
	@Inject
	CommandPlugins(Config conf)
	{
		super("!PLUGINS", "Manage plugins");
		this.requiresChannel(conf.admin_channels);
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length <= 2)
		{
			source.reply("Currently loaded plugins:");

			for (Plugin pl : pluginManager.getPlugins())
				source.reply("  " + pl.pname + " (" + pl.getName() + ")");
		}
		else if (params[1].equalsIgnoreCase("load"))
		{
			String[] pinfo = params[2].split(":");
			if (pinfo.length != 3)
			{
				source.reply("Use group:artifact:version");
				return;
			}

			try
			{
				moo.stopPlugins();
				Plugin p = pluginManager.loadPlugin(pinfo[0], pinfo[1], pinfo[2]);
				moo.buildInjector();
				source.reply("Plugin " + p.getName() + " loaded");
			}
			catch (Exception ex)
			{
				source.reply("Unable to load plugin " + params[2] + ": " + ex.getMessage());
				
				logger.warn("Unable to load plugin " + params[2], ex);
			}
		}
		else if (params[1].equalsIgnoreCase("unload"))
		{
			Plugin p = pluginManager.findPlugin(params[2]);
			if (p == null)
			{
				source.reply("Plugin " + params[2] + " is not loaded");
			}
			else
			{
				pluginManager.remove(p);
				moo.stopPlugins();
				moo.buildInjector();
				source.reply("Plugin " + p.getName() + " removed");
			}
		}
		else if (params[1].equalsIgnoreCase("reload"))
		{
			Plugin p = pluginManager.findPlugin(params[2]);
			if (p == null)
			{
				source.reply("Plugin " + params[2] + " is not loaded");
				return;
			}

			Artifact a = p.getArtifact();
			pluginManager.remove(p);
			moo.stopPlugins();
			moo.buildInjector();

			try
			{
				moo.stopPlugins();
				p = pluginManager.loadPlugin(a.getGroupId(), a.getArtifactId(), a.getVersion());
				moo.buildInjector();
				source.reply("Plugin " + p.getName() + " reloaded");
			}
			catch (Exception ex)
			{
				source.reply("Unable to load plugin " + params[2] + ": " + ex.getMessage());

				logger.warn("Unable to reload plugin " + params[2], ex);
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
