package net.rizon.moo;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.jar.Manifest;
import org.slf4j.Logger;
import org.sonatype.aether.artifact.Artifact;

public abstract class Plugin extends AbstractModule
{
	@Inject
	private static Logger logger;

	protected Artifact artifact;
	private String name, desc;
	public String pname;
	protected ClassLoader loader; // Loader for this plugin
	protected Manifest manifest;

	protected Plugin(String name, String desc)
	{
		this.name = name;
		this.desc = desc;
	}

	public void remove()
	{
		this.stop();

		try
		{
			loader.close();
		}
		catch (IOException ex)
		{
			logger.warn("unable to close classloader", ex);
		}
	}

	public String getName()
	{
		return this.name;
	}

	public String getDescription()
	{
		return this.desc;
	}

	public Manifest getManifest()
	{
		return manifest;
	}

	public Artifact getArtifact()
	{
		return artifact;
	}

	public abstract void start() throws Exception;
	public abstract void stop();

	public abstract List<Command> getCommands();
}
