package net.rizon.moo.plugin.core;

import java.util.logging.Level;

import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.plugin.core.conf.CoreConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventCore extends Event
{
	private static final Logger logger = LoggerFactory.getLogger(EventCore.class);
	
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
			
			logger.warn("Unable to reload core configuration", ex);
		}
	}
}
