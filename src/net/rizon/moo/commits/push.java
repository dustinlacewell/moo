package net.rizon.moo.commits;

import java.util.List;

public interface push
{
	public String getProjectName();
	public List<commit> getCommits();
}