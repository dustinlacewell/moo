package net.rizon.moo.protocol;

import com.google.inject.Inject;
import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;

/* /stats o */
public class Message243 extends Message
{
	@Inject
	private ServerManager serverManager;

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

		Server s = serverManager.findServerAbsolute(message.getSource());
		if (s == null)
		{
			s = new Server(message.getSource());
			serverManager.insertServer(s);
		}
		s.olines_work.put(oper, message.getParams()[5]);
	}
}