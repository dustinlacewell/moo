package net.rizon.moo.commits;

import net.rizon.moo.Moo;
import net.rizon.moo.MPackage;

public class commits extends MPackage
{
	protected static Server s;
	
	public commits()
	{
		super("Commits", "Manages and shows commits made to repositories");
		
		s = new Server(Moo.conf.getCommitsIP(), Moo.conf.getCommitsPort());	
		s.start();
		
		new EventCommit();
	}
}