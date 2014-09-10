package net.rizon.moo.protocol;

import net.rizon.moo.Channel;
import net.rizon.moo.Membership;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.User;

public class Message353 extends Message
{
	public Message353()
	{
		super("353");
	}

	private void parseNamesEntry(Channel c, String entry)
	{
		// &@culex!culex@localghost
		int offset = 0;

		for (char ch : entry.toCharArray())
		{
			if (!Moo.protocol.isCUSDisplayCharacter(ch))
				break;

			offset++;
		}

		String cus = entry.substring(0, offset);
		String nuh = entry.substring(offset);

		User user = Moo.users.findOrCreateUser(nuh);
		Membership mem = c.findUser(user);
		if (mem == null)
		{
			mem = new Membership(user, c);
			user.addChannel(mem);
			c.addUser(mem);
		}

		mem.setStatus(Moo.protocol.CUSDisplayCharacterToEnumSet(cus));
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 4)
			return;

		/* The incoming JOIN should have created the channel already. */
		Channel c = Moo.channels.find(message[2]);
		for (String entry : message[3].split(" "))
			parseNamesEntry(c, entry);
	}
}
