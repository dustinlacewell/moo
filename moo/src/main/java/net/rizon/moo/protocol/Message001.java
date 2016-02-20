package net.rizon.moo.protocol;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.User;
import net.rizon.moo.events.OnConnect;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.IRC;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.ServerManager;

public class Message001 extends Message
{
	@Inject
	private IRC irc;

	@Inject
	private Protocol protocol;
	
	@Inject
	private EventBus eventBus;

	@Inject
	private ServerManager serverManager;
	
	public Message001()
	{
		super("001");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (Moo.conf.general.oper != null)
			protocol.write("OPER", Moo.conf.general.oper.name, Moo.conf.general.oper.pass);
		if (Moo.conf.general.nickserv != null)
			protocol.privmsg("NickServ", "IDENTIFY " + Moo.conf.general.nickserv.pass);

		Moo.me = new User(Moo.conf.general.nick);
		irc.insertUser(Moo.me);

		for (String s : Moo.conf.channels)
			protocol.join(s);

		protocol.write("MAP");
		protocol.write("LINKS");

		for (Server s : serverManager.getServers())
		{
			if (s.isServices())
				continue;

			serverManager.requestStats(s);
			protocol.write("VERSION", s.getName());
		}

		eventBus.post(new OnConnect());
	}
}
