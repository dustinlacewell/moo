package net.rizon.moo.watch;

import java.util.LinkedList;

import net.rizon.moo.mpackage;

public class watch extends mpackage
{
	public watch()
	{
		super("Watch", "Disallows nicks to be used");
		
		new eventWatch();
		new messageNotice();
		new messagePrivmsg();
		new commandWatch(this);
		new watchMonitor();
	}
	
	public static LinkedList<watchEntry> watches = new LinkedList<watchEntry>();
}
