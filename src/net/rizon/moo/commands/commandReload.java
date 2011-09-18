package net.rizon.moo.commands;

import net.rizon.moo.command;
import net.rizon.moo.config;
import net.rizon.moo.moo;

public class commandReload extends command
{
	public commandReload()
	{
		super("!RELOAD", "Reloads the configuration file");
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