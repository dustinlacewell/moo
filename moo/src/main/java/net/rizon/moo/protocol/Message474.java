package net.rizon.moo.protocol;

import com.google.inject.Inject;
import java.util.HashSet;

import net.rizon.moo.Message;
import net.rizon.moo.irc.Protocol;

public class Message474 extends Message
{
	@Inject
	private Protocol protocol;
	
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
			protocol.privmsg("ChanServ", "UNBAN " + message[1]);
			protocol.privmsg("ChanServ", "INVITE " + message[1]);
			protocol.join(message[1]);
			this.invited.add(message[1]);
		}
	}
}
