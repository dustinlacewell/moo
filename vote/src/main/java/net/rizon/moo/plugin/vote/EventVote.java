package net.rizon.moo.plugin.vote;

import java.util.logging.Level;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.plugin.vote.conf.VoteConfiguration;

class EventVote extends Event
{
	@Override
	public void onReload(CommandSource source)
	{
		try
		{
			vote.conf = VoteConfiguration.load();
		}
		catch (Exception ex)
		{
			source.reply("Error reloading vote configuration: " + ex.getMessage());
			vote.log.log(Level.WARNING, "Unable to reload vote configuration", ex);
		}
	}

}
