package net.rizon.moo.plugin.core;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Config;
import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

class CommandReload extends Command
{
	public CommandReload(Plugin pkg)
	{
		super(pkg, "!RELOAD", "Reloads the configuration file");
		this.requiresChannel(Moo.conf.getList("admin_channels"));
	}
	
	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !RELOAD");
		source.notice("!RELOAD reloads the configuration file, moo.properties.");
	}
	
	@Override
	public void execute(CommandSource source, String[] params)
	{
		try
		{
			Config c = new Config();
			for (Event e : Event.getEvents())
				e.onReload(c);

			Moo.conf = c;
			source.reply("Successfully reloaded configuration");
		}
		catch (Exception ex)
		{
			source.reply("Error reloading configuration: " + ex.getMessage());
		}
	}
}