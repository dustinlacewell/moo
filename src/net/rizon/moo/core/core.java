package net.rizon.moo.core;

import net.rizon.moo.MPackage;

public class core extends MPackage
{
	public core()
	{
		super("Commands", "Core commands");
		
		new CommandHelp(this);
		new CommandHost(this);
		new CommandRand(this);
		new CommandReload(this);
		new CommandShell(this);
		new CommandShutdown(this);
		new CommandStatus(this);
	}
}
