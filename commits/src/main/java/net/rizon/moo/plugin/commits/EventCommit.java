package net.rizon.moo.plugin.commits;

import java.util.logging.Level;

import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.plugin.commits.conf.CommitsConfiguration;

class EventCommit extends Event
{
	@Override
	public void onShutdown()
	{
		commits.s.shutdown();
	}

	/**
	 * Reloads the Configuration of commits.
	 * @param source Origin of the target that the !RELOAD command originated from.
	 */
	@Override
	public void onReload(CommandSource source)
	{
		try
		{
			commits.conf = CommitsConfiguration.load();
		}
		catch (Exception ex)
		{
			source.reply("Error reloading commits configuration: " + ex.getMessage());
			
			commits.logger.warn("Unable to reload commits configuration", ex);
		}
	}
}