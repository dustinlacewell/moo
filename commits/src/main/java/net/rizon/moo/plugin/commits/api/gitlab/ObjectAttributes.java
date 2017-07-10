package net.rizon.moo.plugin.commits.api.gitlab;

import java.util.List;

public class ObjectAttributes
{

	private String updated_at;
	private String created_at;
	private String title;
	private String url;
	private String state;
	private String action;
	private String noteable_type;
	private String commit_id;
	private String note;
	private String noteable_id;
	private String target_branch;
	private String iid;
	private Target target;
	private String status;
	private List<String> stages;
	private int duration;

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

	public String getAction()
	{
		return action;
	}

	public String getNotableType()
	{
		return noteable_type;
	}

	public String getCommitId()
	{
		return commit_id;
	}

	public String getNote()
	{
		return note;
	}

	public String getNotableId()
	{
		return noteable_id;
	}

	public String getTargetBranch()
	{
		return target_branch;
	}

	public String getIid()
	{
		return iid;
	}

	public Target getTarget()
	{
		return target;
	}

	public String getStatus()
	{
		return status;
	}

	public List<String> getStages()
	{
		return stages;
	}

	public int getDuration()
	{
		return duration;
	}
}
