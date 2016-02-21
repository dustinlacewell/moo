package net.rizon.moo.plugin.commands.map;

import com.google.inject.Inject;
import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;

public class Message211 extends Message
{
	@Inject
	private ServerManager serverManager;

	public Message211()
	{
		super("211");
	}

	/*
	 * 0: moo
	 * 1: services.rizon.net[unknown@255.255.255.255]
	 * 2: 0  // Buf length
	 * 3: 24 // send.messages
	 * 4: 1  // send.bytes
	 * 5: 40 // recv.messages
	 * 6: 2  // recv.bytes
	 * 7: 48 0 TS GLN TBURST SVS UNKLN KLN KNOCK ENCAP CHW IE EX TS6 EOB QS
	 */
	@Override
	public void run(IRCMessage message)
	{
		long bytes = Long.parseLong(message.getParams()[2]);
		Server serv = serverManager.findServerAbsolute(message.getSource());
		if (serv == null)
		{
			serv = new Server(message.getSource());
			serverManager.insertServer(serv);
		}
		serv.bytes += bytes;
	}
}
