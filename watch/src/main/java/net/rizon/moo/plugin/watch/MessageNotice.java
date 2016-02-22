package net.rizon.moo.plugin.watch;

import com.google.inject.Inject;
import java.util.Iterator;

import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;

class MessageNotice extends Message
{
	@Inject
	private watch watch;
	
	MessageNotice()
	{
		super("NOTICE");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getSource().startsWith("NickServ!"))
		{
			final String msg = message.getParams()[1].replace("\2", "");
			String[] msg_params = msg.split(" ");

			for (Iterator<WatchEntry> it = watch.watches.iterator(); it.hasNext();)
			{
				WatchEntry e = it.next();

				if (e.nick.equalsIgnoreCase(msg_params[0]) && msg.startsWith(e.nick + " is "))
				{
					e.registered = WatchEntry.registeredState.RS_REGISTERED;
					watch.handleWatch(e);
					break;
				}
				else if (msg.endsWith(" isn't registered.") && e.nick.equalsIgnoreCase(msg_params[1]))
				{
					e.registered = WatchEntry.registeredState.RS_NOT_REGISTERED;
					watch.handleWatch(e);
					break;
				}
			}
		}
	}
}
