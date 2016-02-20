package net.rizon.moo.protocol;

import com.google.inject.Inject;
import java.util.Date;
import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;

/* LINKS */
public class Message364 extends Message
{
	@Inject
	private ServerManager serverManager;

	public Message364()
	{
		super("364");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getParams().length < 3)
			return;

		String from = message.getParams()[1];
		String to = message.getParams()[2];

		if (from.equals(to))
			return;

		Server sfrom = serverManager.findServerAbsolute(from);
		if (sfrom == null)
		{
			sfrom = new Server(from);
			serverManager.insertServer(sfrom);
		}

		Server sto = serverManager.findServerAbsolute(to);
		if (sto == null)
		{
			sto = new Server(to);
			serverManager.insertServer(sto);
		}

		if (sfrom != sto)
			sfrom.uplink = sto;
		else
			serverManager.root = sfrom;

		sfrom.link(sto);
		sto.link(sfrom);

		serverManager.last_link = new Date();
	}
}