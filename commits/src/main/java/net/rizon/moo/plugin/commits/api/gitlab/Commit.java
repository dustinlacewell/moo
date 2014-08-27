package net.rizon.moo.plugin.commits.api.gitlab;

public class Commit implements net.rizon.moo.plugin.commits.Commit
{
	private String message;
	private String id;
	private Author author;
	private String url;

	@Override
	public String getBranch()
	{
		return null;
	}

	@Override
	public String getAuthor()
	{
		return author.name + " <" + author.email + ">";
	}

	@Override
	public String[] getMessage()
	{
		return message.trim().split("\n");
	}

	@Override
	public String getRevision()
	{
		return id;
	}

	@Override
	public String getUrl()
	{
		return url;
	}
}
