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
	
	public ClassLoader(String p)
	{
		super(new URL[0]);
		this.plugin = p;
		
		// ok, this is a little bad.
		File f = new File(plugin + "/target/" + plugin + "-2.0-SNAPSHOT-jar-with-dependencies.jar");
		if (!f.exists())
			f = new File(plugin + "/target/" + plugin + "-2.0-SNAPSHOT.jar");
		if (!f.exists())
			f = new File("../" + plugin + "/target/" + plugin + "-2.0-SNAPSHOT-jar-with-dependencies.jar");
		if (!f.exists())
			f = new File("../" + plugin + "/target/" + plugin + "-2.0-SNAPSHOT.jar");
		
		if (f.exists())
			try
			{
				this.addURL(f.toURI().toURL());
			}
			catch (MalformedURLException ex)
			{
				log.log(Level.WARNING, "Unable to add plugin jar for " + p, ex);
			}
		else
			log.log(Level.WARNING, "Unable to locate jar for plugin " + p);
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
