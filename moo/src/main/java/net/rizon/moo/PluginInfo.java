package net.rizon.moo;

import java.util.jar.Manifest;

/**
 *
 * @author Orillion {@literal <orillion@rizon.net>}
 */
public final class PluginInfo
{
	private final Manifest manifest;

	PluginInfo(Manifest manifest)
	{
		this.manifest = manifest;
	}

	/**
	 * Returns the author of this plugin.
	 * <p>
	 * @return Plugin author name.
	 */
	public String getAuthor()
	{
		if (this.manifest == null)
		{
			return "";
		}

		return this.manifest.getMainAttributes().getValue("Author");
	}

	/**
	 * Returns the branch of which this plugin was made.
	 * <p>
	 * @return Name of the branch this plugin was built from.
	 */
	public String getGitBranch()
	{
		if (this.manifest == null)
		{
			return "";
		}

		return this.manifest.getMainAttributes().getValue("Git-Branch");
	}

	/**
	 * Returns the git tag name (if any) of this plugin.
	 * <p>
	 * @return Git tag name of the plugin.
	 */
	public String getGitTag()
	{
		if (this.manifest == null)
		{
			return "";
		}

		return this.manifest.getMainAttributes().getValue("Git-Tag");
	}

	/**
	 * Returns the git revision (long version) of this plugin.
	 * <p>
	 * @return Git revision (long).
	 */
	public String getGitRevision()
	{
		if (this.manifest == null)
		{
			return "";
		}

		return this.manifest.getMainAttributes().getValue("Git-Revision");
	}

	/**
	 * Returns the git revision (short version) of this plugin.
	 * <p>
	 * @return Git revision (short).
	 */
	public String getGitRevisionShort()
	{
		if (this.manifest == null)
		{
			return "";
		}

		return this.manifest.getMainAttributes().getValue("Git-Revision-Short");
	}

	/**
	 * Returns the name of the author who made the commit this plugin was built
	 * from.
	 * <p>
	 * @return Git author name.
	 */
	public String getGitAuthor()
	{
		if (this.manifest == null)
		{
			return "";
		}

		return this.manifest.getMainAttributes().getValue("Git-Author");
	}

	/**
	 * Returns the email address of the author who made the commit this plugin
	 * was built from.
	 * <p>
	 * @return Git author email.
	 */
	public String getGitAuthorEmail()
	{
		if (this.manifest == null)
		{
			return "";
		}

		return this.manifest.getMainAttributes().getValue("Git-Author-Email");
	}
}
