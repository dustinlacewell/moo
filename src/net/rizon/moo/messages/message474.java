package net.rizon.moo.messages;

import java.util.HashSet;

import net.rizon.moo.message;
import net.rizon.moo.moo;

public class message474 extends message
{
	public message474()
	{
		super("474");
	}
	
	private HashSet<String> invited = new HashSet<String>();

	@Override
	public void run(String source, String[] message)
	{
		if (this.invited.contains(message[1]))
		{
			this.invited.remove(message[1]);
			return;
		}
		else if (message.length > 1)
		{
			moo.sock.privmsg("ChanServ", "UNBAN " + message[1]);
			moo.sock.privmsg("ChanServ", "INVITE " + message[1]);
			moo.sock.join(message[1]);
			this.invited.add(message[1]);
		}
	}
}