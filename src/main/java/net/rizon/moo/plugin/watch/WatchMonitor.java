package net.rizon.moo.plugin.watch;

import java.util.Date;
import java.util.Iterator;

import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Timer;

class message_303 extends Message
{
	public message_303()
	{
		super("303");
	}

	@Override
	public void run(String source, String[] message)
	{
		WatchMonitor.request--;

		if (message.length > 1)
			for (Iterator<WatchEntry> it = watch.watches.iterator(); it.hasNext();)
			{
				WatchEntry e = it.next();
				
				for (final String nick : message[1].split(" "))
					if (e.nick.equalsIgnoreCase(nick))
					{
						e.handleWatch();
						break;
					}
			}
		
		if (WatchMonitor.request == 0)
		{
			for (Iterator<WatchEntry> it = watch.watches.iterator(); it.hasNext();)
			{
				WatchEntry e = it.next();

				if (e.handled == false)
					e.handleOffline();
			}
		}
	}
}

class WatchMonitor extends Timer
{
	private static final message_303 message303 = new message_303();
	
	public WatchMonitor()
	{
		super(60, true);
		this.start();
	}
	
	@Override
	public void stop()
	{
		message303.remove();
		super.stop();
	}
	
	public static int request;

	@Override
	public void run(Date now)
	{
		String buffer = "";
		int count = 0;
		
		request = 0;
		
		for (Iterator<WatchEntry> it = watch.watches.iterator(); it.hasNext();)
		{
			WatchEntry e = it.next();
			
			if (e.expires.before(now))
			{
				it.remove();
				continue;
			}
			
			e.handled = false;
			buffer += e.nick + " ";
			++count;
			
			if (buffer.length() > 450 || count >= 16)
			{
				request++;
				Moo.sock.write("ISON :" + buffer);
				buffer = "";
				count = 0;
			}
		}
		
		if (buffer.isEmpty() == false)
		{
			request++;
			Moo.sock.write("ISON :" + buffer);
			buffer = "";
			count = 0;
		}
	}
}

