package net.rizon.moo.protocol;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import net.rizon.moo.irc.Channel;
import net.rizon.moo.irc.Membership;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.irc.User;
import net.rizon.moo.events.EventKick;

public class MessageKick extends Message
{
	@Inject
	private EventBus eventBus;
	
	public MessageKick()
	{
		super("KICK");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 2)
			return;

		User u = Moo.users.find(message[1]);
		Channel c = Moo.channels.find(message[0]);
		if (u != null && c != null)
		{
			Membership mem = u.findChannel(c);
			if (mem != null)
			{
				c.removeUser(mem);
				u.removeChannel(mem);
			}
		}

		eventBus.post(new EventKick(source, message[1], message[0]));
	}
}