package net.rizon.moo.protocol;

import net.rizon.moo.message;
import net.rizon.moo.moo;

public class messagePing extends message
{
	public messagePing()
	{
		super("PING");
	}

	@Override
	public void run(String source, String[] message)
	{
		moo.sock.write("PONG :" + message[0]);
	}
}
