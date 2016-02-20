package net.rizon.moo.protocol;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import java.util.Arrays;

import net.rizon.moo.irc.Channel;
import net.rizon.moo.irc.ChannelUserStatus;
import net.rizon.moo.irc.Membership;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.irc.User;
import net.rizon.moo.events.EventMode;

public class MessageMode extends Message
{
	@Inject
	private EventBus eventBus;
	
	public MessageMode()
	{
		super("MODE");
	}

	private final void handleModes(Channel c, String[] modes)
	{
		if (c == null)
			return;

		boolean add = true;
		int offset = 1;

		for (char m : modes[0].toCharArray())
		{
			switch (m)
			{
				case '+':
				case '-':
					add = (m == '+');
					continue;
				default:
					break;
			}

			ChannelUserStatus cus = Moo.protocol.modeToCUS(m);
			if (cus != null)
			{
				User u = Moo.users.find(modes[offset++]);
				if (u == null)
					continue;

				Membership mem = u.findChannel(c);
				if (mem == null)
					continue;

				if (add)
					mem.addStatus(cus);
				else
					mem.removeStatus(cus);
			}
		}
	}

	@Override
	public void run(String source, String[] message)
	{
		String modes = "";
		for (int i = 1; i < message.length; ++i)
			modes += message[i] + " ";
		modes = modes.trim();

		handleModes(Moo.channels.find(message[0]), Arrays.copyOfRange(message, 1, message.length));

		eventBus.post(new EventMode(source, message[0], modes));
	}
}
