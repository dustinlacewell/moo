package net.rizon.moo.protocol;

import com.google.inject.Inject;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
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
	public void run(String source, String[] message)
	{
		if (message.length > 1 && message[0].equalsIgnoreCase(Moo.me.getNick()))
			if (Moo.conf.channelsContains(message[1]))
				protocol.join(message[1]);
	}
}