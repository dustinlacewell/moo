package net.rizon.moo.protocol;

import com.google.inject.Inject;
import net.rizon.moo.Message;
import net.rizon.moo.irc.Protocol;

public class MessagePing extends Message
{
	@Inject
	private Protocol protocol;
	
	public MessagePing()
	{
		super("PING");
	}

	@Override
	public void run(String source, String[] message)
	{
		protocol.write("PONG", message[0]);
	}
}
