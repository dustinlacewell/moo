package net.rizon.moo.core;

import net.rizon.moo.command;
import net.rizon.moo.config;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

public class commandReload extends command
{
	public commandReload(mpackage pkg)
	{
		super(pkg, "!RELOAD", "Reloads the configuration file");
		this.requiresChannel(moo.conf.getAdminChannels());
	}
	
	@Override
	public void onHelp(String source)
	{
		moo.notice(source, "Syntax: !RELOAD");
		moo.notice(source, "!RELOAD reloads the configuration file, moo.properties.");
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		try
		{
			config c = new config();
			c.load();
			moo.conf = c;
			moo.reply(source, target, "Successfully reloaded configuration");
		}
		catch (Exception ex)
		{
			moo.reply(source, target, "Error reloading configuration: " + ex.getMessage());
		}
	}
}