package net.rizon.moo.servermonitor;

import java.util.Iterator;
import java.util.Random;

import net.rizon.moo.event;
import net.rizon.moo.moo;
import net.rizon.moo.server;

public class eventSplit extends event
{
	@Override
	public void onServerLink(server serv, server to)
	{
		boolean pypsd = serv.getName().startsWith("py") && serv.getName().endsWith(".rizon.net");
		
		if (moo.conf.getDisableSplitMessage() == false && pypsd == false)
			for (int i = 0; i < moo.conf.getSplitChannels().length; ++i)
				moo.privmsg(moo.conf.getSplitChannels()[i], "\2" + serv.getName() + " introduced by " + to.getName() + "\2");
		if (moo.conf.getSplitEmail().isEmpty() == false && pypsd == false)
			mail.send(moo.conf.getSplitEmail(), "Server introduced", serv.getName() + " introduced by " + to.getName());
	}
	
	private static final String[] pypsdMockery = new String[]{
		"Another one bites the dust.",
		"Nothing to see here, move along.",
		"OH MY GOD! I totally couldn't see that coming.",
		"pypsd -- reinventing stability",
		"pypsd -- Splitting faster than a speeding neutrino.",
		"I'm sort of split whether I should make a sarcastic remark about this or not.",
		"Not even the Force could've prevented this.",
		"In much more important news, I'm gonna crash soon.",
		"You thought this was an important message, didn't you?"
	};
	private static final Random rand = new Random();
	
	@Override
	public void onServerSplit(server serv, server from)
	{
		boolean pypsd = serv.getName().startsWith("py") && serv.getName().endsWith(".rizon.net");
		
		if (moo.conf.getDisableSplitMessage() == false)
		{
			if (pypsd)
				for (final String channel : moo.conf.getDevChannels())
				{
					final String insult = pypsdMockery[rand.nextInt(pypsdMockery.length)];
					moo.privmsg(channel, "\2" + serv.getName() + " split from " + from.getName() + "\2. " + insult);
				}
			else
			{
				for (final String channel : moo.conf.getSplitChannels())
					moo.privmsg(channel, "\2" + serv.getName() + " split from " + from.getName() + "\2");
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
		}
		
		if (moo.conf.getSplitEmail().isEmpty() == false && pypsd == false)
			mail.send(moo.conf.getSplitEmail(), "Server split", serv.getName() + " split from " + from.getName());
		
		if (moo.conf.getDisableSplitReconnect() == false && serv.isServices() == false)
		{
			reconnector r = new reconnector(serv, from);
			r.start();
		}
	}
}
