package net.rizon.moo.core;

import net.rizon.moo.Command;
import net.rizon.moo.Config;
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
	public void onHelp(String source)
	{
		Moo.notice(source, "Syntax: !RELOAD");
		Moo.notice(source, "!RELOAD reloads the configuration file, moo.properties.");
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		try
		{
			Moo.conf = new Config();
			Moo.reply(source, target, "Successfully reloaded configuration");
		}
		catch (Exception ex)
		{
			Moo.reply(source, target, "Error reloading configuration: " + ex.getMessage());
		}
	}
}