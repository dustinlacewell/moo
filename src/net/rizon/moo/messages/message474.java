package net.rizon.moo.messages;

import net.rizon.moo.message;
import net.rizon.moo.moo;

public class message474 extends message
{
	public message474()
	{
		super("474");
	}
	
	private String lastchan = "";

	@Override
	public void run(String source, String[] message)
	{
		if (message.length > 1 && message[1].equalsIgnoreCase(this.lastchan) == false)
		{
			moo.sock.privmsg("ChanServ", "UNBAN " + message[1]);
			moo.sock.privmsg("ChanServ", "INVITE " + message[1]);
			moo.sock.join(message[1]);
			this.lastchan = message[1];
		}
	}
}