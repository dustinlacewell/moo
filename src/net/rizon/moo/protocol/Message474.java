package net.rizon.moo.protocol;

import java.util.HashSet;

import net.rizon.moo.Message;
import net.rizon.moo.Moo;

public class Message474 extends Message
{
	public Message474()
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
			Moo.privmsg("ChanServ", "UNBAN " + message[1]);
			Moo.privmsg("ChanServ", "INVITE " + message[1]);
			Moo.join(message[1]);
			this.invited.add(message[1]);
		}
	}
}
