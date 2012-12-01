package net.rizon.moo.commits;

public interface commit
{
	public String getBranch();
	public String getAuthor();
	public String[] getMessage();
	public String getRevision();
}