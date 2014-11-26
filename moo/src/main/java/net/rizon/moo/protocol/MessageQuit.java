package net.rizon.moo.protocol;

import net.rizon.moo.Event;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.User;


public class MessageQuit extends Message
{
	public MessageQuit()
	{
		super("QUIT");
	}

	@Override
	public void run(String source, String[] message)
	{
		for (Event e : Event.getEvents())
			e.onQuit(source, message[0]);

		User u = Moo.users.find(source);
		if (u != null)
			Moo.users.quit(u);
	}
}