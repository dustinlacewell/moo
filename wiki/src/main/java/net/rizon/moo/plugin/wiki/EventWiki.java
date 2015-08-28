package net.rizon.moo.plugin.wiki;

import java.util.logging.Level;

import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.plugin.wiki.conf.WikiConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventWiki extends Event
{
	private static final Logger logger = LoggerFactory.getLogger(EventWiki.class);
	
	@Override
	public void onReload(CommandSource source)
	{
		// TODO: Config gets reloaded, but it's not used. Current implementation requires restart.
		try
		{
			wiki.conf = WikiConfiguration.load();
		}
		catch (Exception ex)
		{
			source.reply("Error reloading wiki configuration: " + ex.getMessage());
			
			logger.warn("Unable to reload configuration", ex);
		}
	}
}
