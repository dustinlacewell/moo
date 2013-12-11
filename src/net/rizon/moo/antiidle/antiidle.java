package net.rizon.moo.antiidle;

import net.rizon.moo.MPackage;

public class antiidle extends MPackage
{
	public antiidle()
	{
		super("Anti idle", "Prevents users from idling in channels");
		
		new CommandIdle(this);
		
		new eventAntiIdle();
		new MessageUserhost();
	}
}