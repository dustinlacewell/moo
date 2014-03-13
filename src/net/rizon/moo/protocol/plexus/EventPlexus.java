package net.rizon.moo.protocol.plexus;

import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Server;

class EventPlexus extends Event
{
	@Override
	public void onNotice(final String source, final String channel, final String message)
	{
		if (source.equals(Moo.conf.getString("nickserv_host")))
		{
			if (message.indexOf("This nickname is registered") != -1 && Moo.conf.getString("nickserv_pass").isEmpty() == false)
				Moo.privmsg(source, "IDENTIFY " + Moo.conf.getString("nickserv_pass"));
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
				
				serv.link(to);
				to.link(serv);
				
				for (Event e : Event.getEvents())
					e.onServerLink(serv, to);
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
				
				serv.link(to);
				to.link(serv);
				
				for (Event e : Event.getEvents())
					e.onServerLink(serv, to);
			}
			else if (message.indexOf("split from") != -1)
			{
				String[] tokens = message.split(" ");
				Server from = Server.findServerAbsolute(tokens[7]);
				if (from != null)
					from.links.remove(tokens[4]);
				
				Server serv = Server.findServerAbsolute(tokens[4]);
				if (serv == null)
					serv = new Server(tokens[4]);
				serv.split(tokens[7]);
				
				for (Event e : Event.getEvents())
					e.onServerSplit(serv, from);
			}
		}
	}
}
