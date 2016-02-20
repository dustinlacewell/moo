package net.rizon.moo.protocol;

import com.google.inject.Inject;
import net.rizon.moo.irc.Channel;
import net.rizon.moo.irc.Membership;
import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.IRC;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.User;
import net.rizon.moo.util.irc.Mask;
import net.rizon.moo.util.irc.MaskParser;

public class Message353 extends Message
{
	@Inject
	private IRC irc;

	@Inject
	private Protocol protocol;

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
			if (!protocol.isCUSDisplayCharacter(ch))
				break;

			offset++;
		}

		String cus = entry.substring(0, offset);
		String nuh = entry.substring(offset);
		Mask mask = MaskParser.parse(nuh);

		User user = irc.findUser(mask.getNick());
		if (user == null)
		{
			user = new User(mask.getNick());
			irc.insertUser(user);
		}

		Membership mem = c.findUser(user);
		if (mem == null)
		{
			mem = new Membership(user, c);
			user.addChannel(mem);
			c.addUser(mem);
		}

		mem.setStatus(protocol.CUSDisplayCharacterToEnumSet(cus));
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getParams().length < 4)
			return;

		/* The incoming JOIN should have created the channel already. */
		Channel c = irc.findChannel(message.getParams()[2]);
		for (String entry : message.getParams()[3].split(" "))
			parseNamesEntry(c, entry);
	}
}
