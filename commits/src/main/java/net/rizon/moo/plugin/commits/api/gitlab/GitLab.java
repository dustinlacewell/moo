package net.rizon.moo.plugin.commits.api.gitlab;

import java.util.LinkedList;
import java.util.List;

import net.rizon.moo.plugin.commits.Push;

public class GitLab implements Push
{
	private String user_name; // pusher
	private String ref; // refs/heads/branch
	private List<Commit> commits;
	private Repository repository;
	
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

	@Override
	public String getBranch()
	{
		return ref.substring(11);
	}

	@Override
	public String getPusher()
	{
		return this.user_name;
	}
}
