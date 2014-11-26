package net.rizon.moo.protocol;

import net.rizon.moo.Channel;
import net.rizon.moo.Event;
import net.rizon.moo.Membership;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.User;

public class MessageJoin extends Message
{
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

		for (Event e : Event.getEvents())
			e.onJoin(source, message[0]);
	}
}