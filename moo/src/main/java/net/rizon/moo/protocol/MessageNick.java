package net.rizon.moo.protocol;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.events.EventNickChange;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.IRC;
import net.rizon.moo.irc.User;

public class MessageNick extends Message
{
	@Inject
	private IRC irc;

	@Inject
	private EventBus eventBus;
	
	public MessageNick()
	{
		super("NICK");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getParams().length != 1)
			return;

		String to = message.getParams()[0];

		eventBus.post(new EventNickChange(message.getSource(), to));

		User source = irc.findUser(message.getNick());
		Moo.users.renameUser(Moo.users.find(source), to);
	}
}