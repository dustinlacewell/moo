package net.rizon.moo.protocol;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import net.rizon.moo.irc.Channel;
import net.rizon.moo.irc.Membership;
import net.rizon.moo.Message;
import net.rizon.moo.events.EventJoin;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.IRC;
import net.rizon.moo.irc.User;

public class MessageJoin extends Message
{
	@Inject
	private IRC irc;
	
	@Inject
	private EventBus eventBus;
	
	public MessageJoin()
	{
		super("JOIN");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getParams().length != 1)
			return;

		String channelName = message.getParams()[0];

		Channel c = irc.findChannel(channelName);
		if (c == null)
		{
			c = new Channel(channelName);
			irc.insertChannel(c);
		}

		User u = irc.findUser(message.getNick());
		if (u == null)
		{
			u = new User(message.getNick());
			irc.insertUser(u);
		}

		Membership mem = new Membership(u, c);

		c.addUser(mem);
		u.addChannel(mem);

		eventBus.post(new EventJoin(message.getSource(), channelName));
	}
}