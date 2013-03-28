package net.rizon.moo.protocol.plexus;

import net.rizon.moo.event;
import net.rizon.moo.moo;
import net.rizon.moo.server;

class eventPlexus extends event
{
	@Override
	public void onNotice(final String source, final String channel, final String message)
	{
		if (source.equals(moo.conf.getNickServHost()))
		{
			if (message.indexOf("This nickname is registered") != -1 && moo.conf.getNickServPass() != null && moo.conf.getNickServPass().isEmpty() == false)
				moo.privmsg(source, "IDENTIFY " + moo.conf.getNickServPass());
		}
		else if (source.indexOf('@') == -1)
		{
			if (message.indexOf("being introduced by") != -1)
			{
				String[] tokens = message.split(" ");
				
				server serv = server.findServerAbsolute(tokens[4]);
				if (serv == null)
				{
					serv = new server(tokens[4]);
					moo.sock.write("MAP");
				}
				else
					serv.splitDel(tokens[8]);
				serv.link(tokens[8]);
				
				server to = server.findServerAbsolute(tokens[8]);
				if (to == null)
					to = new server(tokens[8]);
				to.link(tokens[4]);
				
				for (event e : event.getEvents())
					e.onServerLink(serv, to);
			}
			else if (message.indexOf("End of burst from") != -1)
			{
				String[] tokens = message.split(" ");
				server serv = server.findServerAbsolute(tokens[7]);
				if (serv == null)
				{
					serv = new server(tokens[7]);
					moo.sock.write("MAP");
				}
				else
					serv.splitDel(source);
				serv.link(source);
				
				server to = server.findServerAbsolute(source);
				if (to == null)
					to = new server(source);
				to.link(tokens[7]);
				
				for (event e : event.getEvents())
					e.onServerLink(serv, to);
			}
			else if (message.indexOf("split from") != -1)
			{
				String[] tokens = message.split(" ");
				server from = server.findServerAbsolute(tokens[7]);
				if (from != null)
					from.links.remove(tokens[4]);
				
				server serv = server.findServerAbsolute(tokens[4]);
				if (serv == null)
					serv = new server(tokens[4]);
				serv.split(tokens[7]);
				
				for (event e : event.getEvents())
					e.onServerSplit(serv, from);
			}
		}
	}
}
