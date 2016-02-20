package net.rizon.moo.protocol;

import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Server;

/* /stats c */
public class Message213 extends Message
{
	public Message213()
	{
		super("213");
	}

	@Override
	public void run(IRCMessage message)
	{
		Server serv = Server.findServerAbsolute(source);
		if (serv == null)
			serv = new Server(source);

		if (message.length > 4)
			serv.clines_work.add(message[4]);
	}
}