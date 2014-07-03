package net.rizon.moo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

public abstract class Plugin
{
	private String name, desc;
	public String pname;
	protected ClassLoader loader; // Loader for this class
	public LinkedList<Command> commands = new LinkedList<Command>();
	
	protected Plugin(String name, String desc)
	{
		this.name = name;
		this.desc = desc;

		plugins.add(this);
	}
	
	public void remove()
	{
		this.stop();
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
	
	public abstract void start() throws Exception;
	public abstract void stop();
	
	private static final String base = "net.rizon.moo.plugin.";
	
	public static Plugin loadPlugin(String base, String name) throws Throwable
	{
		Plugin p = findPlugin(name);
		if (p != null)
			return p;
		
		ClassLoader cl = new ClassLoader(base + name, ClassLoader.class.getClassLoader());
		Class<?> c = cl.loadClass(base + name + "." + name);
		Constructor<?> con = c.getConstructor();
		try
		{
			p = (Plugin) con.newInstance();
		}
		catch (InvocationTargetException ex)
		{
			throw ex.getCause();
		}
		
		p.pname = name;
		p.loader = cl;
		
		p.start();
		return p;
	}
	
	public static Plugin loadPlugin(String name) throws Throwable
	{
		return loadPlugin(base, name);
	}
	
	private static LinkedList<Plugin> plugins = new LinkedList<Plugin>();
	
	public static final Plugin[] getPlugins()
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