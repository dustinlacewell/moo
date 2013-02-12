package net.rizon.moo.commits;

import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

public class commits extends mpackage
{
	protected static server s;
	
	public commits()
	{
		super("Commits", "Manages and shows commits made to repositories");
		
		s = new server(moo.conf.getCommitsIP(), moo.conf.getCommitsPort());	
		s.start();
		
		new eventCommit();
	}
}