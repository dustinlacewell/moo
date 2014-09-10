package net.rizon.moo.protocol;

import net.rizon.moo.Channel;
import net.rizon.moo.ChannelUserStatus;
import net.rizon.moo.Event;
import net.rizon.moo.Membership;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.User;

import java.util.Arrays;

public class MessageMode extends Message
{
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
		
		for (Event e : Event.getEvents())
			e.onMode(source, message[0], modes);
	}
}
