package net.rizon.moo.protocol;

import net.rizon.moo.Message;
import net.rizon.moo.Server;

/* LINKS */
public class Message364 extends Message
{
	public Message364()
	{
		super("364");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 3)
			return;

		String from = message[1];
		String to = message[2];
		
		if (from.equals(to))
			return;
		
		Server sfrom = Server.findServerAbsolute(from);
		if (sfrom == null)
			sfrom = new Server(from);

		Server sto = Server.findServerAbsolute(to);
		if (sto == null)
			sto = new Server(to);

		if (sfrom != sto)
			sfrom.uplink = sto;
		else
			Server.root = sfrom;
		sfrom.link(sto);
		sto.link(sfrom);
	}
}