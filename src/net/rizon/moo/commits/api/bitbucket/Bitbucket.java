package net.rizon.moo.commits.api.bitbucket;

import java.util.LinkedList;
import java.util.List;

import net.rizon.moo.commits.Push;

public class Bitbucket implements Push
{
	private List<Commit> commits;
	public static class repository
	{
		public String name;
	}
	private repository repository;
	
	@Override
	public String getProjectName()
	{
		return this.repository.name;
	}
	
	@Override
	public List<net.rizon.moo.commits.Commit> getCommits()
	{
		List<net.rizon.moo.commits.Commit> l = new LinkedList<net.rizon.moo.commits.Commit>();
		for (Commit c : this.commits)
			l.add(c);
		return l;
	}
}