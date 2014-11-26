package net.rizon.moo.plugin.antiidle;

import net.rizon.moo.Message;
import net.rizon.moo.Moo;

class MessageUserhost extends Message
{
	public MessageUserhost()
	{
		super("302");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length != 2)
			return;

		int eq = message[1].indexOf('=');
		if (eq == -1)
			return;

		if (message[1].charAt(eq - 1) == '*')
		{
			AntiIdleEntry.removeTimerFor(message[1].substring(0, eq - 1));
			Moo.mode(antiidle.conf.channel, "+v " + message[1].substring(0, eq - 1));
		}
	}
}