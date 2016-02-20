package net.rizon.moo.protocol;

import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Set;

import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Protocol;

public class Message474 extends Message
{
	@Inject
	private Protocol protocol;
	
	public Message474()
	{
		super("474");
	}

	private final Set<String> invited = new HashSet<>();

	@Override
	public void run(IRCMessage message)
	{
		String channel = message.getParams()[1];

		if (this.invited.contains(channel))
		{
			this.invited.remove(channel);
			return;
		}

		protocol.privmsg("ChanServ", "UNBAN " + channel);
		protocol.privmsg("ChanServ", "INVITE " + channel);
		protocol.join(channel);
		this.invited.add(channel);
	}
}
