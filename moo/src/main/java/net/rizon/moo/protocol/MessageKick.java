package net.rizon.moo.protocol;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import java.util.ArrayList;
import net.rizon.moo.irc.Channel;
import net.rizon.moo.irc.Membership;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.irc.User;
import net.rizon.moo.events.EventKick;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.IRC;

public class MessageKick extends Message
{
	@Inject
	private IRC irc;
	
	@Inject
	private EventBus eventBus;
	
	public MessageKick()
	{
		super("KICK");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getParams().length < 2)
			return;

		User u = irc.findUser(message.getParams()[1]);
		Channel c = irc.findChannel(message.getParams()[0]);
		if (u == null || c == null)
			return;

		Membership mem = u.findChannel(c);
		if (mem != null)
		{
			c.removeUser(mem);
			u.removeChannel(mem);

			if (u.getChannels().isEmpty() && u != Moo.me)
				irc.removeUser(u);
		}

		eventBus.post(new EventKick(message.getSource(), message.getParams()[1], message.getParams()[0]));

		if (u == Moo.me)
		{
			for (Membership membership : new ArrayList<>(c.getUsers()))
			{
				User user = membership.getUser();

				c.removeUser(membership);
				user.removeChannel(membership);

				if (user.getChannels().isEmpty() && user != Moo.me)
					irc.removeUser(user);
			}

			irc.removeChannel(c);
		}
	}
}