package net.rizon.moo.plugin.vote;

import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.vote.conf.Vote;
import net.rizon.moo.plugin.vote.conf.VoteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class vote extends Plugin
{
	private static final Logger logger = LoggerFactory.getLogger(EventVote.class);

	public static VoteConfiguration conf;

	private CommandVote vote;
	private Event e;

	public vote() throws Exception
	{
		super("Vote", "Manages votes");
		conf = VoteConfiguration.load();

		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `votes` (`id` int, `channel` text, `info` text, `owner` text, `date` date, `closed` int)");
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `vote_casts` (`id` int, `channel` text, `voter` text, `vote` text)");
	}

	@Override
	public void start() throws Exception
	{
		vote = new CommandVote(this);
		e = new EventVote();
	}

	@Override
	public void stop()
	{
		vote.remove();
		e.remove();
	}

	protected static String getVoteEmailFor(String chan)
	{
		for (Vote v : conf.vote)
			if (v.channel.equals(chan))
				return v.email;

		return null;
	}
}