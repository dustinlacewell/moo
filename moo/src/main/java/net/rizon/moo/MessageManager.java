package net.rizon.moo;

import com.google.inject.Inject;
import java.util.Set;
import net.rizon.moo.io.IRCMessage;

public class MessageManager
{
	@Inject
	private Set<Message> messages;

	public void run(IRCMessage message)
	{
		for (Message m : messages)
			if (m.getName().equalsIgnoreCase(message.getCommand()))
				m.run(message);
	}
}
