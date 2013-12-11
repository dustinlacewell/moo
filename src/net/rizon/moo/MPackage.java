package net.rizon.moo;

import java.util.LinkedList;

public abstract class MPackage
{
	private String name;
	private String desc;
	private LinkedList<Command> commands = new LinkedList<Command>();

	public MPackage(final String name, final String desc)
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
	
	public void addCommand(Command c)
	{
		this.commands.add(c);
	}
	
	public final LinkedList<Command> getCommands()
	{
		return this.commands;
	}
	
	private static LinkedList<MPackage> packages = new LinkedList<MPackage>();
	
	public static final LinkedList<MPackage> getPackages()
	{
		return packages;
	}
}