package net.rizon.moo.watch;

import java.util.LinkedList;

import net.rizon.moo.MPackage;

public class watch extends MPackage
{
	public watch()
	{
		super("Watch", "Disallows nicks to be used");
		
		new EventWatch();
		new MessageNotice();
		new CommandWatch(this);
		new WatchMonitor();
	}
	
	public static LinkedList<WatchEntry> watches = new LinkedList<WatchEntry>();
}
