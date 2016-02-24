package net.rizon.moo.protocol;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.conf.Config;
import net.rizon.moo.events.EventClientConnect;
import net.rizon.moo.events.EventNotice;
import net.rizon.moo.events.OnServerLink;
import net.rizon.moo.events.OnServerSplit;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;

public class MessageNotice extends Message
{
	private static final Pattern connectPattern = Pattern.compile(".* Client connecting.*: ([^ ]*) \\(~?([^@]*).*?\\) \\[([AaBbCcDdEeFf0-9.:]*)\\] (?:\\{[^}]*\\} )?\\[(.*)\\]");
	
	@Inject
	private Protocol protocol;
	
	@Inject
	private EventBus eventBus;

	@Inject
	private ServerManager serverManager;

	@Inject
	private Config conf;

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
		if (conf.general.nickserv != null && source.equals(conf.general.nickserv.mask))
		{
			if (message.contains("This nickname is registered"))
				protocol.privmsg(source, "IDENTIFY " + conf.general.nickserv.pass);
		}
		else if (source.indexOf('@') == -1)
		{
			if (message.contains("being introduced by"))
			{
				String[] tokens = message.split(" ");

				Server serv = serverManager.findServerAbsolute(tokens[4]),
						to = serverManager.findServerAbsolute(tokens[8]);

				if (to == null)
				{
					to = new Server(tokens[8]);
					serverManager.insertServer(to);
				}
				if (serv == null)
				{
					serv = new Server(tokens[4]);
					serverManager.insertServer(serv);
				}

				serv.splitDel(to);
				serverManager.requestStats(serv);

				serv.uplink = to;
				serv.link(to);
				to.link(serv);

				serverManager.last_link = new Date();

				eventBus.post(new OnServerLink(serv, to));
			}
			else if (message.contains("End of burst from"))
			{
				String[] tokens = message.split(" ");

				Server serv = serverManager.findServerAbsolute(tokens[7]),
						to = serverManager.findServerAbsolute(source);
				if (serv == null)
				{
					serv = new Server(tokens[7]);
					serverManager.insertServer(serv);
				}
				if (to == null)
				{
					to = new Server(source);
					serverManager.insertServer(to);
				}

				serv.splitDel(to);
				serverManager.requestStats(serv);

				serv.uplink = serverManager.root;
				serv.link(to);
				to.link(serv);

				serverManager.last_link = new Date();

				eventBus.post(new OnServerLink(serv, to));
			}
			else if (message.contains("split from"))
			{
				String[] tokens = message.split(" ");

				Server serv = serverManager.findServerAbsolute(tokens[4]), from = serverManager.findServerAbsolute(tokens[7]);
				if (serv == null)
				{
					serv = new Server(tokens[4]);
					serverManager.insertServer(serv);
				}
				if (from == null)
				{
					from = new Server(tokens[7]);
					serverManager.insertServer(from);
				}

				serverManager.split(serv, from);

				serverManager.last_split = new Date();

				eventBus.post(new OnServerSplit(serv, from));
			}
		}
	}
}
