package net.rizon.moo.plugin.commits;

import java.util.List;

public interface Push
{
	public String getProjectName();
	public String getBranch();
	public String getPusher();
	public List<Commit> getCommits();
}