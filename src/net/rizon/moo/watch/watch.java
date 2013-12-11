package net.rizon.moo.watch;

import java.util.LinkedList;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Message;
import net.rizon.moo.Plugin;
import net.rizon.moo.Timer;

public class watch extends Plugin
{
	public static LinkedList<WatchEntry> watches = new LinkedList<WatchEntry>();
	
	private Event e;
	private Message n;
	private Command w;
	private Timer m;
	
	public watch()
	{
		super("Watch", "Disallows nicks to be used");
	}

	@Override
	public void start() throws Exception
	{
		e = new EventWatch();
		n = new MessageNotice();
		w = new CommandWatch(this);
		m = new WatchMonitor();		
	}

	@Override
	public void stop()
	{
		e.remove();
		n.remove();
		w.remove();
		m.stop();
	}
}
