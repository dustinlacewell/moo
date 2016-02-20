package net.rizon.moo.protocol;

import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Server;

/* /stats o */
public class Message243 extends Message
{
	public Message243()
	{
		super("243");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getParams().length < 6)
			return;

		String oper = message.getParams()[4];

		Server s = Server.findServerAbsolute(source);
		if (s == null)
			s = new Server(source);
		s.olines_work.put(oper, message.getParams()[5]);
	}
}