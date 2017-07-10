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
	private Commit commit;
	private String build_id;

	private ObjectAttributes object_attributes;
	private String object_kind;
	private User user;
	private Issue issue;
	private MergeRequest merge_request;

	private String build_name;
	private String build_status;
	private String build_stage;

	private Project project;
	private List<Build> builds;

	@Override
	public String getProjectName()
	{
		// @NOTE(Orillion): Apparently having a consistent payload is hard for
		// gitlab, so we need to grab the name from 3 different places.
		if (this.repository != null)
		{
			/*
			For the following events:
			- PUSH
			- TAG
			- ISSUE
			- COMMENT
			- BUILD
			 */
			return this.repository.name;
		}
		else if (this.project != null)
		{
			/*
			For the following events:
			- PUSH
			- TAG
			- ISSUE
			- COMMENT
			- WIKI
			- PIPELINE
			 */
			return this.project.getName();
		}
		else if (this.object_attributes != null && this.object_attributes.getTarget() != null)
		{
			/*
			For the following events:
			- MERGE
			 */
			return this.object_attributes.getTarget().name;
		}

		return null;
	}

	public Repository getRepository()
	{
		return this.repository;
	}

	public Commit getCommit()
	{
		return commit;
	}

	@Override
	public List<net.rizon.moo.plugin.commits.Commit> getCommits()
	{
		if (this.commits == null)
			return null;

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

	public ObjectAttributes getObjectAttributes()
	{
		return object_attributes;
	}

	public String getObjectKind()
	{
		return object_kind;
	}

	public User getUser()
	{
		return user;
	}

	public Issue getIssue()
	{
		return issue;
	}

	public MergeRequest getMergeRequest()
	{
		return merge_request;
	}

	public String getBuildName()
	{
		return build_name;
	}

	public String getBuildStage()
	{
		return build_stage;
	}

	public String getBuildStatus()
	{
		return build_status;
	}

	public String getBuildId()
	{
		return build_id;
	}

	public Project getProject()
	{
		return project;
	}

	public List<Build> getBuilds()
	{
		return builds;
	}

	public PipelineStatus getPipelineStatus()
	{
		String status = this.getObjectAttributes().getStatus();

		if ("success".equalsIgnoreCase(status))
		{
			return PipelineStatus.SUCCESS;
		}
		else if ("pending".equalsIgnoreCase(status))
		{
			return PipelineStatus.PENDING;
		}
		else if ("created".equalsIgnoreCase(status))
		{
			return PipelineStatus.CREATED;
		}
		else if ("failed".equalsIgnoreCase(status))
		{
			return PipelineStatus.FAILED;
		}
		else if ("running".equalsIgnoreCase(status))
		{
			return PipelineStatus.RUNNING;
		}
		else if ("skipped".equalsIgnoreCase(status))
		{
			return PipelineStatus.SKIPPED;
		}

		return PipelineStatus.UNKNOWN;
	}
}
