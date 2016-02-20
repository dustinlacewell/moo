package net.rizon.moo.protocol;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import net.rizon.moo.irc.Channel;
import net.rizon.moo.irc.Membership;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.irc.User;
import net.rizon.moo.events.EventPart;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.IRC;

public class MessagePart extends Message
{
	@Inject
	private IRC irc;
	
	@Inject
	private EventBus eventBus;
	
	public MessagePart()
	{
		super("PART");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getParams().length < 1)
			return;

		User u = irc.findUser(message.getNick());
		Channel c = irc.findChannel(message.getParams()[0]);
		if (u != null && c != null)
		{
			Membership mem = u.findChannel(c);
			if (mem != null)
			{
				c.removeUser(mem);
				u.removeChannel(mem);
			}
		}

		eventBus.post(new EventPart(message.getSource(), message.getParams()[0]));
	}
}
