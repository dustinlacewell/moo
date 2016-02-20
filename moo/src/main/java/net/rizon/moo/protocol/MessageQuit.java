package net.rizon.moo.protocol;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import net.rizon.moo.Message;
import net.rizon.moo.irc.User;
import net.rizon.moo.events.EventQuit;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Channel;
import net.rizon.moo.irc.IRC;
import net.rizon.moo.irc.Membership;


public class MessageQuit extends Message
{
	@Inject
	private IRC irc;

	@Inject
	private EventBus eventBus;
	
	public MessageQuit()
	{
		super("QUIT");
	}

	@Override
	public void run(IRCMessage message)
	{
		eventBus.post(new EventQuit(message.getSource(), message.getParams()[0]));

		User u = irc.findUser(message.getNick());
		if (u == null)
			return;

		List<Membership> mems = new ArrayList<>(u.getChannels());
		for (Membership m : mems)
		{
			Channel c = m.getChannel();

			u.removeChannel(m);
			c.removeUser(m);
		}

		irc.removeUser(u);
	}
}