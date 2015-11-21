package net.rizon.moo.protocol;

import net.rizon.moo.Channel;
import net.rizon.moo.Membership;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.User;
import net.rizon.moo.events.EventPart;

public class MessagePart extends Message
{
	public MessagePart()
	{
		super("PART");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 1)
			return;

		User u = Moo.users.find(source);
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

		Moo.getEventBus().post(new EventPart(source, message[0]));
	}
}
