package net.rizon.moo.protocol;

import com.google.inject.Inject;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Protocol;

public class MessageInvite extends Message
{
	@Inject
	private Protocol protocol;
	
	public MessageInvite()
	{
		super("INVITE");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getParams().length > 1 && message.getParams()[0].equalsIgnoreCase(Moo.me.getNick()))
			if (Moo.conf.channelsContains(message.getParams()[1]))
				protocol.join(message.getParams()[1]);
	}
}