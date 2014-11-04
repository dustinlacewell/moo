package net.rizon.moo.plugin.core;

import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.plugin.core.conf.CoreConfiguration;

import java.util.logging.Level;

public class EventCore extends Event
{
	@Override
	public void onReload(CommandSource source)
	{
		try
		{
			core.conf = CoreConfiguration.load();
		}
		catch (Exception ex)
		{
			source.reply("Error reloading core configuration: " + ex.getMessage());
			core.log.log(Level.WARNING, "Unable to reload core configuration", ex);
		}
	}
}
