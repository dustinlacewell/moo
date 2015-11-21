package net.rizon.moo.plugin.watch;

import io.netty.util.concurrent.ScheduledFuture;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import net.rizon.moo.Command;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

public class watch extends Plugin
{
	public static LinkedList<WatchEntry> watches = new LinkedList<WatchEntry>();

	private Message watchReply;
	private EventWatch e;
	private Message n;
	private Command w;
	private ScheduledFuture watchMonitor;

	public watch()
	{
		super("Watch", "Disallows nicks to be used");
	}

	@Override
	public void start() throws Exception
	{
		e = new EventWatch();
		Moo.getEventBus().register(e);
		
		n = new MessageNotice();
		w = new CommandWatch(this);
		watchMonitor = Moo.scheduleWithFixedDelay(new WatchMonitor(), 1, TimeUnit.MINUTES);
		watchReply = new WatchReply();
	}

	@Override
	public void stop()
	{
		e = new EventWatch();
		Moo.getEventBus().unregister(e);
		
		n.remove();
		w.remove();
		watchMonitor.cancel(false);
		watchReply.remove();
	}
}
