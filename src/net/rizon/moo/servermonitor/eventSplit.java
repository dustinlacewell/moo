package net.rizon.moo.servermonitor;

import java.util.Iterator;

import net.rizon.moo.event;
import net.rizon.moo.moo;
import net.rizon.moo.server;

public class eventSplit extends event
{
	@Override
	public void onServerLink(server serv, server to)
	{
		if (serv.getName().startsWith("py") && serv.getName().endsWith(".rizon.net"))
			return;
		
		if (moo.conf.getDisableSplitMessage() == false)
			for (int i = 0; i < moo.conf.getSplitChannels().length; ++i)
				moo.privmsg(moo.conf.getSplitChannels()[i], "\2" + serv.getName() + " introduced by " + to.getName() + "\2");
		if (moo.conf.getSplitEmail().isEmpty() == false)
			mail.send(moo.conf.getSplitEmail(), "Server introduced", serv.getName() + " introduced by " + to.getName());
	}
	
	@Override
	public void onServerSplit(server serv, server from)
	{
		if (serv.getName().startsWith("py") && serv.getName().endsWith(".rizon.net"))
			return;
		
		if (moo.conf.getDisableSplitMessage() == false)
		{
			for (int i = 0; i < moo.conf.getSplitChannels().length; ++i)
				moo.privmsg(moo.conf.getSplitChannels()[i], "\2" + serv.getName() + " split from " + from.getName() + "\2");
			for (server s : server.getServers())
			{
				if (s.isHub() == true && s.getSplit() == null)
					for (Iterator<String> it2 = s.clines.iterator(); it2.hasNext();)
					{
						String cline = it2.next();
						
						if (serv.getName().equalsIgnoreCase(cline))
							for (int i = 0; i < moo.conf.getSplitChannels().length; ++i)
								moo.privmsg(moo.conf.getSplitChannels()[i], serv.getName() + " can connect to " + s.getName());
					}
			}
		}
		if (moo.conf.getSplitEmail().isEmpty() == false)
			mail.send(moo.conf.getSplitEmail(), "Server split", serv.getName() + " split from " + from.getName());
	}
}
