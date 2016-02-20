package net.rizon.moo.protocol;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import java.util.Arrays;

import net.rizon.moo.irc.Channel;
import net.rizon.moo.irc.ChannelUserStatus;
import net.rizon.moo.irc.Membership;
import net.rizon.moo.Message;
import net.rizon.moo.irc.User;
import net.rizon.moo.events.EventMode;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.IRC;
import net.rizon.moo.irc.Protocol;

public class MessageMode extends Message
{
	@Inject
	private IRC irc;

	@Inject
	private Protocol protocol;
	
	@Inject
	private EventBus eventBus;
	
	public MessageMode()
	{
		super("MODE");
	}

	private void handleModes(Channel c, String[] modes)
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

			ChannelUserStatus cus = protocol.modeToCUS(m);
			if (cus != null)
			{
				User u = irc.findUser(modes[offset++]);
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
	public void run(IRCMessage message)
	{
		String modes = "";
		for (int i = 1; i < message.getParams().length; ++i)
			modes += message.getParams()[i] + " ";
		modes = modes.trim();

		Channel c = irc.findChannel(message.getParams()[0]);
		handleModes(c, Arrays.copyOfRange(message.getParams(), 1, message.getParams().length));

		eventBus.post(new EventMode(message.getSource(), message.getParams()[0], modes));
	}
}
