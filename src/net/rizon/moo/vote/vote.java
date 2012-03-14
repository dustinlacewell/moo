package net.rizon.moo.vote;

import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

public class vote extends mpackage
{
	public vote()
	{
		super("Vote", "Manages votes");
		
		new commandVote(this);
		
		moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `votes` (`id` int, `channel` text, `info` text, `owner` text, `date` date, `closed` int)");
		moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `vote_casts` (`id` int, `channel` text, `voter` text, `vote` text)");
	}
}