package net.rizon.moo.protocol;

import net.rizon.moo.Message;
import net.rizon.moo.Moo;

public class MessagePing extends Message
{
	public MessagePing()
	{
		super("PING");
	}

	@Override
	public void run(String source, String[] message)
	{
		Moo.write("PONG", message[0]);
	}
}
