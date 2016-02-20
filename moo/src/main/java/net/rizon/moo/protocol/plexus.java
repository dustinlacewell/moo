package net.rizon.moo.protocol;

import com.google.common.eventbus.Subscribe;
import com.google.inject.multibindings.Multibinder;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Server;
import net.rizon.moo.events.EventNotice;
import net.rizon.moo.events.OnServerLink;
import net.rizon.moo.events.OnServerSplit;

public class plexus extends ProtocolPlugin
{
	public plexus()
	{
		super("Plexus", "Plexus protocol functions");
	}
	

	@Override
	protected void configure()
	{
		Multibinder<Message> messageBinder = Multibinder.newSetBinder(binder(), Message.class);
		
		messageBinder.addBinding().to(Message001.class);
		messageBinder.addBinding().to(Message015.class);
		messageBinder.addBinding().to(Message017.class);
		messageBinder.addBinding().to(Message213.class);
		messageBinder.addBinding().to(Message219.class);
		messageBinder.addBinding().to(Message243.class);
		messageBinder.addBinding().to(Message303.class);
		messageBinder.addBinding().to(Message353.class);
		messageBinder.addBinding().to(Message364.class);
		messageBinder.addBinding().to(Message401.class);
		messageBinder.addBinding().to(Message474.class);
		messageBinder.addBinding().to(MessageInvite.class);
		messageBinder.addBinding().to(MessagePing.class);
		messageBinder.addBinding().to(MessagePrivmsg.class);
		messageBinder.addBinding().to(MessageJoin.class);
		messageBinder.addBinding().to(MessagePart.class);
		messageBinder.addBinding().to(MessageKick.class);
		messageBinder.addBinding().to(MessageMode.class);
		messageBinder.addBinding().to(MessageNick.class);
		messageBinder.addBinding().to(MessageNotice.class);
		messageBinder.addBinding().to(MessageQuit.class);
		messageBinder.addBinding().to(MessageWallops.class);
	}

	@Override
	public void start() throws Exception
	{
		Moo.getEventBus().register(this);
	}

	@Override
	public void stop()
	{
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