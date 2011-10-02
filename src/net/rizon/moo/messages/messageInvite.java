package net.rizon.moo.messages;

import net.rizon.moo.message;
import net.rizon.moo.moo;

public class messageInvite extends message
{
	public messageInvite()
	{
		super("INVITE");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length > 1 && message[0].equalsIgnoreCase(moo.conf.getNick()))
		{
			if (moo.conf.getChannels() != null)
				for (int i = 0; i < moo.conf.getChannels().length; ++i)
					if (moo.conf.getChannels()[i].equalsIgnoreCase(message[1]))
					{
						moo.sock.join(message[1]);
						break;
					}
			if (moo.conf.getIdleChannels() != null)
				for (int i = 0; i < moo.conf.getIdleChannels().length; ++i)
					if (moo.conf.getIdleChannels()[i].equalsIgnoreCase(message[1]))
					{
						moo.sock.join(message[1]);
						break;
					}
		}
	}
}