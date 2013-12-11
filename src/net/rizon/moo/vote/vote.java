package net.rizon.moo.vote;

import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

public class vote extends Plugin
{
	private CommandVote vote;
	
	public vote()
	{
		super("Vote", "Manages votes");
		
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `votes` (`id` int, `channel` text, `info` text, `owner` text, `date` date, `closed` int)");
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `vote_casts` (`id` int, `channel` text, `voter` text, `vote` text)");
	}

	@Override
	public void start() throws Exception
	{
		vote = new CommandVote(this);
	}

	@Override
	public void stop()
	{
		vote.remove();
	}
}