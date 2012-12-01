package net.rizon.moo.commits.api.bitbucket;

import java.util.LinkedList;
import java.util.List;

import net.rizon.moo.commits.push;

public class bitbucket implements push
{
	private List<commit> commits;
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
	public List<net.rizon.moo.commits.commit> getCommits()
	{
		List<net.rizon.moo.commits.commit> l = new LinkedList<net.rizon.moo.commits.commit>();
		for (commit c : this.commits)
			l.add(c);
		return l;
	}
}