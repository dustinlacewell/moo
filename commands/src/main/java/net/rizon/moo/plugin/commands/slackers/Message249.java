package net.rizon.moo.plugin.commands.slackers;

import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;

public class Message249 extends Message
{
	public Message249()
	{
		super("249");
	}

	@Override
	public void run(IRCMessage message)
	{
		String[] m = message.getParams()[2].split(" ");
		if (m.length != 5)
			return;

		String oper = m[1];
		CommandSlackers.opers.add(oper);
	}
}
