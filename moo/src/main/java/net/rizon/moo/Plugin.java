package net.rizon.moo;

import com.google.inject.AbstractModule;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.Manifest;

public abstract class Plugin extends AbstractModule
{
	/*private static final Logger log = Logger.getLogger(Plugin.class.getName());*/
	private static LinkedList<Plugin> plugins = new LinkedList<Plugin>();

	private String name, desc;
	private PluginInfo info;
	public String pname;
	protected ClassLoader loader; // Loader for this plugin
	//public List<Command> commands = new LinkedList<>();

	protected Plugin(String name, String desc)
	{
		/*
		 In case people try to get plugininfo during construction.
		 Will just return empty strings for everything.
		 */
		this.info = new PluginInfo(null);
		this.name = name;
		this.desc = desc;
	}

	public void remove()
	{
		this.stop();
		/*try
		{
			loader.close();
		}
		catch (IOException ex)
		{
			log.log(Level.WARNING, "Unable to close classloader when removing plugin", ex);
		}*/
		plugins.remove(this);
	}

	public final String getName()
	{
		return this.name;
	}

	public final String getDescription()
	{
		return this.desc;
	}

	public final PluginInfo getPluginInfo()
	{
		return this.info;
	}

	public abstract void start() throws Exception;
	public abstract void stop();

	public abstract List<Command> getCommands();

	public static Manifest findPluginManifest(Class pluginClass)
	{
		String className = pluginClass.getSimpleName() + ".class";
		String classPath = pluginClass.getResource(className).toString();

		if (!classPath.startsWith("jar"))
		{
			// Class not loaded from a jar file.
			return null;
		}

		String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1)
				+ "/META-INF/MANIFEST.MF";

		URL url;

		try
		{
			url = new URL(manifestPath);
		}
		catch (MalformedURLException e)
		{
			return null;
		}

		try (InputStream manifestInputStream = url.openStream())
		{
			return new Manifest(manifestInputStream);
		}
		catch (IOException e)
		{
			return null;
		}
	}

	//@SuppressWarnings("resource")
	private static Plugin loadPlugin(String base, String name, boolean core) throws Throwable
	{
		Plugin p = findPlugin(name);
		if (p != null)
			return p;

		ClassLoader cl = core ? new ClassLoader(base + name) : new ClassLoader(name, base + name);
		Class<?> c = cl.loadClass(base + name + "." + name);
		Constructor<?> con = c.getConstructor();
		try
		{
			p = (Plugin) con.newInstance();
		}
		catch (InvocationTargetException ex)
		{
			//cl.close();
			throw ex.getCause();
		}

		p.info = new PluginInfo(findPluginManifest(c));

		plugins.add(p);
		p.pname = name;
		p.loader = cl;

		return p;
	}

	public static Plugin loadPlugin(String name) throws Throwable
	{
		return loadPlugin("net.rizon.moo.plugin.", name, false);
	}

	public static Plugin loadPluginCore(String base, String name) throws Throwable
	{
		return loadPlugin(base, name, true);
	}

	public static Plugin[] getPlugins()
	{
		Plugin[] a = new Plugin[plugins.size()];
		plugins.toArray(a);
		return a;
	}

	public static Plugin findPlugin(String name)
	{
		for (Plugin p : plugins)
			if (p.pname.equals(name))
				return p;
		return null;
	}
}
