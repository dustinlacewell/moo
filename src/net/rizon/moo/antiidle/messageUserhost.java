package net.rizon.moo.antiidle;

import net.rizon.moo.message;
import net.rizon.moo.moo;

class messageUserhost extends message
{
	public messageUserhost()
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
			antiIdleEntry.removeTimerFor(message[1].substring(0, eq - 1));
			moo.mode(moo.conf.getAntiIdleChannel(), "+v " + message[1].substring(0, eq - 1));
		}
	}
}