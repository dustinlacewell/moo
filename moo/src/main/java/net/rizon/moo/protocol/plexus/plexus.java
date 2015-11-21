package net.rizon.moo.protocol.plexus;

import com.google.common.eventbus.Subscribe;
import net.rizon.moo.Event;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Server;
import net.rizon.moo.events.EventNotice;
import net.rizon.moo.events.OnServerLink;
import net.rizon.moo.events.OnServerSplit;
import net.rizon.moo.protocol.Message001;
import net.rizon.moo.protocol.Message213;
import net.rizon.moo.protocol.Message219;
import net.rizon.moo.protocol.Message243;
import net.rizon.moo.protocol.Message303;
import net.rizon.moo.protocol.Message353;
import net.rizon.moo.protocol.Message364;
import net.rizon.moo.protocol.Message401;
import net.rizon.moo.protocol.Message474;
import net.rizon.moo.protocol.MessageInvite;
import net.rizon.moo.protocol.MessageJoin;
import net.rizon.moo.protocol.MessageKick;
import net.rizon.moo.protocol.MessageMode;
import net.rizon.moo.protocol.MessageNick;
import net.rizon.moo.protocol.MessageNotice;
import net.rizon.moo.protocol.MessagePart;
import net.rizon.moo.protocol.MessagePing;
import net.rizon.moo.protocol.MessagePrivmsg;
import net.rizon.moo.protocol.MessageQuit;
import net.rizon.moo.protocol.MessageWallops;
import net.rizon.moo.protocol.ProtocolPlugin;

public class plexus extends ProtocolPlugin
{
	private Message m001, m213, m219, m243, m303, m353, m364, m401, m474, invite, ping, privmsg, join, part,
					kick, mode, nick, notice, quit, wallops,
					m015, m017;

	public plexus()
	{
		super("Plexus", "Plexus protocol functions");
	}

	@Override
	public void start() throws Exception
	{
		/* Core */
		m001 = new Message001();
		m213 = new Message213();
		m219 = new Message219();
		m243 = new Message243();
		m303 = new Message303();
		m353 = new Message353();
		m364 = new Message364();
		m401 = new Message401();
		m474 = new Message474();
		invite = new MessageInvite();
		ping = new MessagePing();
		privmsg = new MessagePrivmsg();
		join = new MessageJoin();
		part = new MessagePart();
		kick = new MessageKick();
		mode = new MessageMode();
		nick = new MessageNick();
		notice = new MessageNotice();
		quit = new MessageQuit();
		wallops = new MessageWallops();

		/* Plexus */
		m015 = new Message015();
		m017 = new Message017();

		Moo.getEventBus().register(this);
	}

	@Override
	public void stop()
	{
		m001.remove();
		m213.remove();
		m219.remove();
		m243.remove();
		m303.remove();
		m353.remove();
		m364.remove();
		m401.remove();
		m474.remove();
		invite.remove();
		ping.remove();
		privmsg.remove();
		join.remove();
		part.remove();
		kick.remove();
		mode.remove();
		nick.remove();
		notice.remove();
		quit.remove();
		wallops.remove();

		m015.remove();
		m017.remove();

		Moo.getEventBus().unregister(this);
	}
	
	@Subscribe
	public void onNotice(EventNotice evt)
	{
		String source = evt.getSource(), message = evt.getMessage();
		
		if (Moo.conf.general.nickserv != null && source.equals(Moo.conf.general.nickserv.mask))
		{
			if (message.indexOf("This nickname is registered") != -1)
				Moo.privmsg(source, "IDENTIFY " + Moo.conf.general.nickserv.pass);
		}
		else if (source.indexOf('@') == -1)
		{
			if (message.indexOf("being introduced by") != -1)
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

				Moo.getEventBus().post(new OnServerLink(serv, to));
			}
			else if (message.indexOf("End of burst from") != -1)
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

				Moo.getEventBus().post(new OnServerLink(serv, to));
			}
			else if (message.indexOf("split from") != -1)
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

				Moo.getEventBus().post(new OnServerSplit(serv, from));
			}
		}
	}
}