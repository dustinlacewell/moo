package net.rizon.moo.plugin.vote;


import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.plugin.vote.conf.VoteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EventVote extends Event
{
	private static final Logger logger = LoggerFactory.getLogger(EventVote.class);
	
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
			
			logger.warn("Unable to reload configuration", ex);
		}
	}

}
