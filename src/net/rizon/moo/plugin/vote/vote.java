package net.rizon.moo.plugin.vote;

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
	
	protected static String getVoteEmailFor(String chan)
	{
		for (String s : Moo.conf.getList("vote_email"))
		{
			String[] sp = s.split(":");
			if (sp.length == 2 && sp[0].equalsIgnoreCase(chan))
				return sp[1];
		}
		
		return null;
	}
}