package net.rizon.moo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;

public final class ClassLoader extends URLClassLoader
{
	private static final Logger log = Logger.getLogger(ClassLoader.class.getName());
	private String plugin;

	public ClassLoader(String plugin, String classname)
	{
		super(new URL[0]);

		File jar = null;

		this.plugin = plugin;

		if (plugin != null)
		{
			File targetFolder = new File(plugin + "/target/");
			if (!targetFolder.exists())
				targetFolder = new File("../" + plugin + "/target/");

			/* can't find the directory where we expect it */
			if (!targetFolder.exists())
			{
				log.log(Level.WARNING, "Unable to find " + plugin + "/target/ directory to source the JAR from.");
				return;
			}

			File[] targetFiles = targetFolder.listFiles();

			for (File loadCandidate : targetFiles)
			{
				String name = loadCandidate.getName();

				if (name.endsWith(".jar") && name.startsWith("moo-" + plugin))
				{
					jar = loadCandidate;

					/*
					 * `jar-with-dependencies` is the best load candidate, so
					 * use it straight away, otherwise look for any other matching jar.
					 */
					if (name.endsWith("-jar-with-dependencies.jar"))
						break;
				}
			}

			if (jar == null)
			{
				File f = new File(targetFolder, "classes/");
				if (!f.isDirectory())
				{
					log.log(Level.WARNING, "Unable to locate plugin JAR/class files for " + plugin);
					return;
				}

				log.log(Level.FINE, "Using classes/ for plugin " + plugin);
				try
				{
					this.addURL(f.toURI().toURL());
				}
				catch (MalformedURLException ex)
				{
					log.log(Level.WARNING, "Unable to add class URL for " + plugin + " [" + f.toURI() + "]", ex);
				}
				return;
			}
			else
			{
				log.log(Level.FINE, "Found load candidate " + jar.getName() + " for plugin `" + plugin + "`");
			}
		}
		else
		{
			/* no plugin so use the main jar */
			jar = new File(ClassLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		}

		if (jar.exists())
		{
			try
			{
				this.addURL(jar.toURI().toURL());
			}
			catch (MalformedURLException ex)
			{
				log.log(Level.WARNING, "Unable to add plugin JAR URL for " + plugin + " [" + jar.toURI() + "]", ex);
			}
		}
		else
		{
			log.log(Level.WARNING, "Unable to locate JAR for plugin " + plugin);
		}
	}

	public ClassLoader(String classname)
	{
		this(null, classname);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException
	{
		return loadClassRecurse(name, true);
	}

	private Class<?> loadClassRecurse(String name, boolean recurse) throws ClassNotFoundException
	{
		try
		{
			return super.loadClass(name);
		}
		catch (ClassNotFoundException ex)
		{
			if (recurse)
				for (Plugin p : Plugin.getPlugins())
					try
					{
						return p.loader.loadClassRecurse(name, false);
					}
					catch (ClassNotFoundException ex2)
					{
					}

			throw ex;
		}
	}
}
