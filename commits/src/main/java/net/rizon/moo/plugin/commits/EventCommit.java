package net.rizon.moo.plugin.commits;

import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.plugin.commits.conf.CommitsConfiguration;

import java.util.logging.Level;

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
			commits.log.log(Level.WARNING, "Unable to reload commits configuration", ex);
		}
	}
}