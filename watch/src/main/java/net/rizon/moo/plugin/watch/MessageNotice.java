package net.rizon.moo.plugin.watch;

import java.util.Iterator;

import net.rizon.moo.Message;

class MessageNotice extends Message
{
	public MessageNotice()
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
			
			for (Iterator<WatchEntry> it = watch.watches.iterator(); it.hasNext();)
			{
				WatchEntry e = it.next();
				
				if (e.nick.equalsIgnoreCase(msg_params[0]) && msg.startsWith(e.nick + " is "))
				{
					e.registered = WatchEntry.registeredState.RS_REGISTERED;
					e.handleWatch();
					break;
				}
				else if (msg.endsWith(" isn't registered.") && e.nick.equalsIgnoreCase(msg_params[1]))
				{
					e.registered = WatchEntry.registeredState.RS_NOT_REGISTERED;
					e.handleWatch();
					break;
				}
			}
		}
	}
}
