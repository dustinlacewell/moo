package net.rizon.moo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public final class ClassLoader extends URLClassLoader
{
	private static final Logger logger = LoggerFactory.getLogger(ClassLoader.class);

	private URL url;
	private PluginManager pluginManager;

	public ClassLoader(PluginManager pluginManager, File jar) throws MalformedURLException
	{
		super(new URL[0]);

		url = jar.toURI().toURL();
		this.addURL(jar.toURI().toURL());
		this.pluginManager = pluginManager;
	}

	public void addFile(File f) throws MalformedURLException
	{
		this.addURL(f.toURI().toURL());
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
			if (recurse && name.contains("Oline"))
			{
				ex.printStackTrace();
				int i = 5;
			}
			if (recurse)
				for (Plugin p : pluginManager.getPlugins())
					try
					{
						return p.loader.loadClassRecurse(name, false);
					}
					catch (ClassNotFoundException ex2)
					{
					}

			if (recurse && name.contains("Oline"))
			{
				int i = 5;
			}
			throw ex;
		}
	}
}
