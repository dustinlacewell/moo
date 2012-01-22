package net.rizon.moo;

import java.util.LinkedList;

public abstract class mpackage
{
	private String name;
	private String desc;
	private LinkedList<command> commands = new LinkedList<command>();

	public mpackage(final String name, final String desc)
	{
		this.name = name;
		this.desc = desc;
		packages.add(this);
	}
	
	public final String getPackageName()
	{
		return this.name;
	}
	
	public final String getDescription()
	{
		return this.desc;
	}
	
	public void addCommand(command c)
	{
		this.commands.add(c);
	}
	
	public final LinkedList<command> getCommands()
	{
		return this.commands;
	}
	
	private static LinkedList<mpackage> packages = new LinkedList<mpackage>();
	
	public static final LinkedList<mpackage> getPackages()
	{
		return packages;
	}
}