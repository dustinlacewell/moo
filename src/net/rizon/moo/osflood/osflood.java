package net.rizon.moo.osflood;

import net.rizon.moo.MPackage;

public class osflood extends MPackage
{
	public osflood()
	{
		super("OSFlood", "Detects and akills users flooding OperServ");
		
		new EventOSFlood();
	}
	
}
