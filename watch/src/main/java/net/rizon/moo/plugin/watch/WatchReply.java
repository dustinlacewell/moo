package net.rizon.moo.plugin.watch;

import com.google.inject.Inject;
import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;


class WatchReply extends Message
{
	@Inject
	private watch watch;
	
	public WatchReply()
	{
		super("303");
	}

	@Override
	public void run(IRCMessage message)
	{
		WatchMonitor.request--;

		if (message.getParams().length > 1)
			for (WatchEntry e : watch.watches)
			{
				for (final String nick : message.getParams()[1].split(" "))
					if (e.nick.equalsIgnoreCase(nick))
					{
						watch.handleWatch(e);
						break;
					}
			}

		if (WatchMonitor.request == 0)
		{
			for (WatchEntry e : watch.watches)
			{
				if (e.handled == false)
					watch.handleOffline(e);
			}
		}
	}
}