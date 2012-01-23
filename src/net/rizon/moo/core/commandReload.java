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
		this.requireAdmin();
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		try
		{
			config c = new config();
			c.load();
			moo.conf = c;
			moo.sock.reply(source, target, "Successfully reloaded configuration");
		}
		catch (Exception ex)
		{
			moo.sock.reply(source, target, "Error reloading configuration: " + ex.getMessage());
		}
	}
}