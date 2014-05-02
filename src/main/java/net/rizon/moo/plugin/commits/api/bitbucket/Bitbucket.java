package net.rizon.moo.plugin.commits.api.bitbucket;

import java.util.LinkedList;
import java.util.List;

import net.rizon.moo.plugin.commits.Push;

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
	public List<net.rizon.moo.plugin.commits.Commit> getCommits()
	{
		List<net.rizon.moo.plugin.commits.Commit> l = new LinkedList<net.rizon.moo.plugin.commits.Commit>();
		for (Commit c : this.commits)
			l.add(c);
		return l;
	}
}