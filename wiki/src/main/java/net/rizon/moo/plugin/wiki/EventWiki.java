package net.rizon.moo.plugin.wiki;

import java.util.logging.Level;

import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.plugin.wiki.conf.WikiConfiguration;

public class EventWiki extends Event
{
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
			wiki.log.log(Level.WARNING, "Unable to reload wiki configuration", ex);
		}
	}
}
