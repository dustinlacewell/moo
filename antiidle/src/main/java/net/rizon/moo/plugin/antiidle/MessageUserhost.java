package net.rizon.moo.plugin.antiidle;

import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.io.IRCMessage;

class MessageUserhost extends Message
{
	public MessageUserhost()
	{
		super("302");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getParams().length != 2)
			return;

		int eq = message.getParams()[1].indexOf('=');
		if (eq == -1)
			return;

		if (message.getParams()[1].charAt(eq - 1) == '*')
		{
			AntiIdleEntry.removeTimerFor(message.getParams()[1].substring(0, eq - 1));
			antiidle.protocol.mode(antiidle.conf.channel, "+v " + message.getParams()[1].substring(0, eq - 1));
		}
	}
}