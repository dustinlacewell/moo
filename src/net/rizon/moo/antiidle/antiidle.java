package net.rizon.moo.antiidle;

import net.rizon.moo.mpackage;

public class antiidle extends mpackage
{
	public antiidle()
	{
		super("Anti idle", "Prevents users from idling in channels");
		
		new commandIdle(this);
		
		new eventAntiIdle();
	}
}