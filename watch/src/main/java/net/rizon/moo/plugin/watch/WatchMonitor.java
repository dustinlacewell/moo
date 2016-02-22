package net.rizon.moo.plugin.watch;

import com.google.inject.Inject;
import java.util.Date;
import java.util.Iterator;

import net.rizon.moo.Moo;
import net.rizon.moo.irc.Protocol;

class WatchMonitor implements Runnable
{
	public static int request;
	
	@Inject
	private watch watch;
	
	@Inject
	private Protocol protocol;

	@Override
	public void run()
	{
		Date now = new Date();
		String buffer = "";
		int count = 0;

		request = 0;

		for (Iterator<WatchEntry> it = watch.watches.iterator(); it.hasNext();)
		{
			WatchEntry e = it.next();

			if (e.expires.before(now))
			{
				watch.remove(e);
				it.remove();
				continue;
			}

			e.handled = false;
			buffer += e.nick + " ";
			++count;

			if (buffer.length() > 450 || count >= 16)
			{
				request++;
				protocol.write("ISON", buffer);
				buffer = "";
				count = 0;
			}
		}

		if (buffer.isEmpty() == false)
		{
			request++;
			protocol.write("ISON", buffer);
			buffer = "";
			count = 0;
		}
	}
}

