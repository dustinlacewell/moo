package net.rizon.moo.protocol;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.events.EventNickChange;

public class MessageNick extends Message
{
	@Inject
	private EventBus eventBus;
	
	public MessageNick()
	{
		super("NICK");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length != 1)
			return;

		eventBus.post(new EventNickChange(source, message[0]));

		Moo.users.renameUser(Moo.users.find(source), message[0]);
	}
}