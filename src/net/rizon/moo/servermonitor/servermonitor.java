package net.rizon.moo.servermonitor;

import net.rizon.moo.mpackage;

public class servermonitor extends mpackage
{
	public servermonitor()
	{
		super("Server Monitor", "Monitor servers");
		
		new commandServer(this);
		new commandSplit(this);
		new eventSplit();
	}
}
