package net.rizon.moo.plugin.watch;

import java.util.Iterator;
import net.rizon.moo.Message;


class WatchReply extends Message
{
	public WatchReply()
	{
		super("303");
	}

	@Override
	public void run(String source, String[] message)
	{
		WatchMonitor.request--;

		if (message.length > 1)
			for (WatchEntry e : watch.watches)
			{
				for (final String nick : message[1].split(" "))
					if (e.nick.equalsIgnoreCase(nick))
					{
						e.handleWatch();
						break;
					}
			}

		if (WatchMonitor.request == 0)
		{
			for (WatchEntry e : watch.watches)
			{
				if (e.handled == false)
					e.handleOffline();
			}
		}
	}
}