package net.rizon.moo.watch;

import java.util.Date;
import java.util.Iterator;

import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.timer;

class message_303 extends message
{
	public message_303()
	{
		super("303");
	}

	@Override
	public void run(String source, String[] message)
	{
		watchMonitor.request--;

		for (Iterator<watchEntry> it = watch.watches.iterator(); it.hasNext();)
		{
			watchEntry e = it.next();
			
			for (final String nick : message)
				if (e.nick.equalsIgnoreCase(nick))
				{
					e.handleWatch();
					break;
				}
		}
		
		if (watchMonitor.request == 0)
		{
			for (Iterator<watchEntry> it = watch.watches.iterator(); it.hasNext();)
			{
				watchEntry e = it.next();

				if (e.handled == false)
					e.handleOffline();
			}
		}
	}
}

public class watchMonitor extends timer
{
	@SuppressWarnings("unused")
	private static final message_303 message303 = new message_303();
	
	public watchMonitor()
	{
		super(60, true);
		this.start();
	}
	
	public static int request;

	@Override
	public void run(Date now)
	{
		String buffer = "";
		int count = 0;
		
		request = 0;
		
		for (Iterator<watchEntry> it = watch.watches.iterator(); it.hasNext();)
		{
			watchEntry e = it.next();
			
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
				moo.sock.write("ISON :" + buffer);
				buffer = "";
				count = 0;
			}
		}
		
		if (buffer.isEmpty() == false)
		{
			request++;
			moo.sock.write("ISON :" + buffer);
			buffer = "";
			count = 0;
		}
	}
}

