package net.rizon.moo.plugin.commits.api.gitlab;

public class Commit implements net.rizon.moo.plugin.commits.Commit
{

	private String message;
	private String id;
	private Author author;
	private String author_name;
	private String author_email;
	private String url;

	public String getSha()
	{
		return id;
	}

	public String getShortSha()
	{
		return id.substring(0, 7);
	}

	@Override
	public String getBranch()
	{
		return null;
	}

	@Override
	public String getAuthor()
	{
		if (author != null)
		{
			return author.name + " <" + author.email + ">";
		}

		return author_name + " <" + author_email + ">";
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
