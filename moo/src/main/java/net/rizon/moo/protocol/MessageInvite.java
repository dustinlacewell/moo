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
		if (message.length > 1 && message[0].equalsIgnoreCase(Moo.conf.getString("nick")))
			if (Moo.conf.listContains("channels", message[1]))
				Moo.join(message[1]);
	}
}