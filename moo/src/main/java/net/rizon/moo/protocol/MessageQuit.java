package net.rizon.moo.protocol;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.irc.User;
import net.rizon.moo.events.EventQuit;


public class MessageQuit extends Message
{
	@Inject
	private EventBus eventBus;
	
	public MessageQuit()
	{
		super("QUIT");
	}

	@Override
	public void run(String source, String[] message)
	{
		eventBus.post(new EventQuit(source, message[0]));

		User u = Moo.users.find(source);
		if (u != null)
			Moo.users.quit(u);
	}
}