package net.rizon.moo.protocol;

import net.rizon.moo.Channel;
import net.rizon.moo.Event;
import net.rizon.moo.Membership;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.User;

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
		
		for (Event e : Event.getEvents())
			e.onPart(source, message[0]);
	}
}
