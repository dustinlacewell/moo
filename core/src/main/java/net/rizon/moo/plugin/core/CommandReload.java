package net.rizon.moo.plugin.core;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.conf.Config;

class CommandReload extends Command
{
	private static final Logger log = Logger.getLogger(CommandReload.class.getName());

	public CommandReload(Plugin pkg)
	{
		super(pkg, "!RELOAD", "Reloads the configuration file");
		this.requiresChannel(Moo.conf.admin_channels);
	}
	
	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !RELOAD");
		source.notice("!RELOAD reloads the configuration file, moo.yml.");
	}
	
	@Override
	public void execute(CommandSource source, String[] params)
	{
		try
		{
			Config c = Config.load("moo.yml", Config.class);
			for (Event e : Event.getEvents())
				e.onReload(source);

			Moo.conf = c;
			source.reply("Successfully reloaded configuration");
		}
		catch (Exception ex)
		{
			source.reply("Error reloading configuration: " + ex.getMessage());
		}
	}
}