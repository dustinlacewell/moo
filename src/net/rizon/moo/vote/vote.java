package net.rizon.moo.vote;

import net.rizon.moo.Moo;
import net.rizon.moo.MPackage;

public class vote extends MPackage
{
	public vote()
	{
		super("Vote", "Manages votes");
		
		new CommandVote(this);
		
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `votes` (`id` int, `channel` text, `info` text, `owner` text, `date` date, `closed` int)");
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `vote_casts` (`id` int, `channel` text, `voter` text, `vote` text)");
	}
}