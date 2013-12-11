package net.rizon.moo.protocol;

import net.rizon.moo.Message;
import net.rizon.moo.Moo;

public class MessageInvite extends Message
{
	public MessageInvite()
	{
		super("INVITE");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length > 1 && message[0].equalsIgnoreCase(Moo.conf.getNick()))
		{
			for (int i = 0; i < Moo.conf.getChannels().length; ++i)
				if (Moo.conf.getChannels()[i].equalsIgnoreCase(message[1]))
				{
					Moo.join(message[1]);
					break;
				}
			for (int i = 0; i < Moo.conf.getIdleChannels().length; ++i)
				if (Moo.conf.getIdleChannels()[i].equalsIgnoreCase(message[1]))
				{
					Moo.join(message[1]);
					break;
				}
		}
	}
}