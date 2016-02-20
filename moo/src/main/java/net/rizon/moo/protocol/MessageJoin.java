package net.rizon.moo.protocol;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import net.rizon.moo.irc.Channel;
import net.rizon.moo.irc.Membership;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.irc.User;
import net.rizon.moo.events.EventJoin;

public class MessageJoin extends Message
{
	@Inject
	private EventBus eventBus;
	
	public MessageJoin()
	{
		super("JOIN");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length != 1)
			return;

		Channel c = Moo.channels.findOrCreateChannel(message[0]);
		User u = Moo.users.findOrCreateUser(source);

		Membership mem = new Membership(u, c);

		c.addUser(mem);
		u.addChannel(mem);

		eventBus.post(new EventJoin(source, message[0]));
	}
}