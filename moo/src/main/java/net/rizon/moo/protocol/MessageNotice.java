package net.rizon.moo.protocol;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.events.EventClientConnect;
import net.rizon.moo.events.EventNotice;
import net.rizon.moo.events.OnServerLink;
import net.rizon.moo.events.OnServerSplit;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;

public class MessageNotice extends Message
{
	private static final Pattern connectPattern = Pattern.compile(".* Client connecting.*: ([^ ]*) \\(~?([^@]*).*?\\) \\[([AaBbCcDdEeFf0-9.:]*)\\] (?:\\{[^}]*\\} )?\\[(.*)\\]");
	
	@Inject
	private Protocol protocol;
	
	@Inject
	private EventBus eventBus;

	public MessageNotice()
	{
		super("NOTICE");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getParams().length < 2)
			return;

		String target = message.getParams()[0], text = message.getParams()[1];

		eventBus.post(new EventNotice(message.getSource(), target, text));
		
		process(message.getSource(), text);

		Matcher m = connectPattern.matcher(text);
		if (m.matches())
		{
			if (message.getNick() != null)
				return; // only from servers

			final String nick = m.group(1), ident = m.group(2), ip = m.group(3), realname = m.group(4);
			eventBus.post(new EventClientConnect(nick, ident, ip, realname));
		}
	}

	// XXX this should be in a plugin
	private void process(String source, String message)
	{
		if (Moo.conf.general.nickserv != null && source.equals(Moo.conf.general.nickserv.mask))
		{
			if (message.contains("This nickname is registered"))
				protocol.privmsg(source, "IDENTIFY " + Moo.conf.general.nickserv.pass);
		}
		else if (source.indexOf('@') == -1)
		{
			if (message.contains("being introduced by"))
			{
				String[] tokens = message.split(" ");

				Server serv = Server.findServerAbsolute(tokens[4]),
						to = Server.findServerAbsolute(tokens[8]);

				if (to == null)
					to = new Server(tokens[8]);
				if (serv == null)
					serv = new Server(tokens[4]);

				serv.splitDel(to);

				serv.uplink = to;
				serv.link(to);
				to.link(serv);

				eventBus.post(new OnServerLink(serv, to));
			}
			else if (message.contains("End of burst from"))
			{
				String[] tokens = message.split(" ");

				Server serv = Server.findServerAbsolute(tokens[7]),
						to = Server.findServerAbsolute(source);
				if (serv == null)
					serv = new Server(tokens[7]);
				if (to == null)
					to = new Server(source);

				serv.splitDel(to);

				serv.uplink = Server.root;
				serv.link(to);
				to.link(serv);

				eventBus.post(new OnServerLink(serv, to));
			}
			else if (message.contains("split from"))
			{
				String[] tokens = message.split(" ");

				Server serv = Server.findServerAbsolute(tokens[4]), from = Server.findServerAbsolute(tokens[7]);
				if (serv == null)
					serv = new Server(tokens[4]);
				if (from == null)
					from = new Server(tokens[7]);

				serv.uplink = null;
				from.links.remove(serv);
				serv.split(from);

				eventBus.post(new OnServerSplit(serv, from));
			}
		}
	}
}
