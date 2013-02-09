package net.rizon.moo.core;

import net.rizon.moo.mpackage;

public class core extends mpackage
{
	public core()
	{
		super("Commands", "Core commands");
		
		new commandHelp(this);
		new commandHost(this);
		new commandRand(this);
		new commandReload(this);
		new commandShell(this);
		new commandShutdown(this);
		new commandStatus(this);
	}
}
