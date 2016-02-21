package net.rizon.moo;

import java.util.ArrayList;
import java.util.List;

public abstract class Command
{
	private Plugin pkg;
	private String cmdname;
	private String description;
	private List<String> channels = new ArrayList<>();

	public Command(Plugin pkg, final String cmdname, final String desc)
	{
		this.pkg = pkg;
		this.cmdname = cmdname;
		this.description = desc;

		//pkg.commands.add(this);
	}

	public Command(String cmdname, String description)
	{
		this.cmdname = cmdname;
		this.description = description;
	}

	public void remove()
	{
		//pkg.commands.remove(this);
	}

	public Plugin getPackage()
	{
		return this.pkg;
	}

	public final String getCommandName()
	{
		return this.cmdname;
	}

	public final String getDescription()
	{
		return this.description;
	}

	protected void requiresChannel(final String[] chans)
	{
		for (String c : chans)
			channels.add(c.toLowerCase());
	}

	public boolean isRequiredChannel(String channel)
	{
		return channels.isEmpty() || channels.contains(channel.toLowerCase());
	}

	public abstract void execute(CommandSource source, String[] params);

	public void onHelpList(CommandSource source)
	{
		if (this.getDescription() != null && this.getDescription().isEmpty() == false)
			source.notice(" " + this.getCommandName() + " - " + this.getDescription());
		else
			source.notice(" " + this.getCommandName());
	}

	public void onHelp(CommandSource source)
	{
		source.notice("No help available.");
	}
}
