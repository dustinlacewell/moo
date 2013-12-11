package net.rizon.moo.commits;

public interface Commit
{
	public String getBranch();
	public String getAuthor();
	public String[] getMessage();
	public String getRevision();
}