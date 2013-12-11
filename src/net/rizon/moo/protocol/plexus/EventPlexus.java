package net.rizon.moo.protocol.plexus;

import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Server;

class EventPlexus extends Event
{
	@Override
	public void onNotice(final String source, final String channel, final String message)
	{
		if (source.equals(Moo.conf.getNickServHost()))
		{
			if (message.indexOf("This nickname is registered") != -1 && Moo.conf.getNickServPass() != null && Moo.conf.getNickServPass().isEmpty() == false)
				Moo.privmsg(source, "IDENTIFY " + Moo.conf.getNickServPass());
		}
		else if (source.indexOf('@') == -1)
		{
			if (message.indexOf("being introduced by") != -1)
			{
				String[] tokens = message.split(" ");
				
				Server serv = Server.findServerAbsolute(tokens[4]);
				if (serv == null)
				{
					serv = new Server(tokens[4]);
					Moo.sock.write("MAP");
				}
				else
					serv.splitDel(tokens[8]);
				serv.link(tokens[8]);
				
				Server to = Server.findServerAbsolute(tokens[8]);
				if (to == null)
					to = new Server(tokens[8]);
				to.link(tokens[4]);
				
				for (Event e : Event.getEvents())
					e.onServerLink(serv, to);
			}
			else if (message.indexOf("End of burst from") != -1)
			{
				String[] tokens = message.split(" ");
				Server serv = Server.findServerAbsolute(tokens[7]);
				if (serv == null)
				{
					serv = new Server(tokens[7]);
					Moo.sock.write("MAP");
				}
				else
					serv.splitDel(source);
				serv.link(source);
				
				Server to = Server.findServerAbsolute(source);
				if (to == null)
					to = new Server(source);
				to.link(tokens[7]);
				
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
