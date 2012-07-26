package net.rizon.moo.watch;

import java.util.Iterator;

import net.rizon.moo.message;

class messageNotice extends message
{
	public messageNotice()
	{
		super("NOTICE");
	}
	
	@Override
	public void run(String source, String[] message)
	{
		if (source.startsWith("NickServ!"))
		{
			final String msg = message[1].replace("\2", "");
			String[] msg_params = msg.split(" ");
			
			for (Iterator<watchEntry> it = watch.watches.iterator(); it.hasNext();)
			{
				watchEntry e = it.next();
				
				if (e.nick.equalsIgnoreCase(msg_params[0]) && msg.startsWith(e.nick + " is "))
				{
					e.registered = watchEntry.registeredState.RS_REGISTERED;
					e.handleWatch();
					break;
				}
				else if (msg.endsWith(" isn't registered.") && e.nick.equalsIgnoreCase(msg_params[1]))
				{
					e.registered = watchEntry.registeredState.RS_NOT_REGISTERED;
					e.handleWatch();
					break;
				}
			}
		}
	}
}
