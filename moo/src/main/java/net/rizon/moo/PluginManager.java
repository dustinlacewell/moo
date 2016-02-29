package net.rizon.moo;

import com.google.inject.Inject;
import com.jcabi.aether.Aether;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.jar.Manifest;
import net.rizon.moo.conf.Config;
import org.slf4j.Logger;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

public class PluginManager
{
	@Inject
	private static Logger logger;

	@Inject
	private Config conf;

	private final List<Plugin> plugins = new ArrayList<>();

	private List<Artifact> resolveArtifacts(Artifact artifact) throws DependencyResolutionException
	{
		File local = new File(conf.plugin_repository);
		Collection<RemoteRepository> remotes = Arrays.asList();

		Aether a = new Aether(remotes, local);

		Collection<Artifact> deps = a.resolve(artifact, "runtime");
		return new ArrayList<>(deps);
	}

	private Manifest getManifest(File jar) throws IOException
	{
		String manifestPath = "jar:file:" + jar.getAbsolutePath() + "!/META-INF/MANIFEST.MF";

		URL url = new URL(manifestPath);

		try (InputStream manifestInputStream = url.openStream())
		{
			return new Manifest(manifestInputStream);
		}
	}

	public Plugin loadPlugin(String groupId, String artifactId, String version) throws Exception
	{
		Artifact a = new DefaultArtifact(groupId, artifactId, "", "jar", version);

		Plugin p = findPlugin(a);
		if (p != null)
			return p;

		List<Artifact> artifacts = resolveArtifacts(a);

		Artifact artifact = artifacts.remove(0);
		// artifacts contains dependencies now

		ClassLoader cl = new ClassLoader(this, artifact.getFile());
		try
		{
			for (Artifact a2 : artifacts)
				cl.addFile(a2.getFile());

			Manifest mf = getManifest(artifact.getFile());

			Class<?> c;
			try
			{
				String mainClass = mf.getMainAttributes().getValue("Main-Class");
				c = cl.loadClass(mainClass);
			}
			catch (ClassNotFoundException ex)
			{
				logger.warn("unable to load main class", ex);
				return null;
			}

			Constructor<?> con = c.getConstructor();

			p = (Plugin) con.newInstance();

			p.artifact = artifact;
			p.loader = cl;
			cl = null;
			p.manifest = mf;
			p.pname = artifactId;

			plugins.add(p);

			return p;
		}
		finally
		{
			if (cl != null)
				cl.close();
		}
	}

	public void remove(Plugin p)
	{
		p.remove();
		plugins.remove(p);
	}

	public Plugin[] getPlugins()
	{
		Plugin[] a = new Plugin[plugins.size()];
		plugins.toArray(a);
		return a;
	}

	public Plugin findPlugin(Artifact artifact)
	{
		for (Plugin p : plugins)
			if (p.artifact.equals(artifact))
				return p;
		return null;
	}

	public Plugin findPlugin(String name)
	{
		for (Plugin p : plugins)
			if (p.pname.equals(name))
				return p;
		return null;
	}
}
