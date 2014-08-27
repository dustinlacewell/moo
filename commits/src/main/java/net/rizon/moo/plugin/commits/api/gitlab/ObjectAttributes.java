package net.rizon.moo.plugin.commits.api.gitlab;

public class ObjectAttributes
{
	private String updated_at;
	private String created_at;
	private String title;
	private String url;
	private String state;

	public String getUpdatedAt()
	{
		return updated_at;
	}
	
	public String getCreatedAt()
	{
		return created_at;
	}

	public String getTitle()
	{
		return title;
	}

	public String getUrl()
	{
		return url;
	}

	public String getState()
	{
		return state;
	}
}
