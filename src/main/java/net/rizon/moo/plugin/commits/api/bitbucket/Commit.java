package net.rizon.moo.plugin.commits.api.bitbucket;

class Commit implements net.rizon.moo.plugin.commits.Commit
{
	private String raw_author;
	private String branch;
	private String message;
	private String node;

	@Override
	public String getBranch()
	{
		return branch;
	}

	@Override
	public String getAuthor()
	{
		return raw_author;
	}

	@Override
	public String[] getMessage()
	{
		return message.trim().split("\n");
	}

	@Override
	public String getRevision()
	{
		return node;
	}
}