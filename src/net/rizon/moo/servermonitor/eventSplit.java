package net.rizon.moo.servermonitor;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.event;
import net.rizon.moo.mail;
import net.rizon.moo.moo;
import net.rizon.moo.server;
import net.rizon.moo.split;
import net.rizon.moo.timer;

class textDelay extends timer
{
	public textDelay()
	{
		super(5, false);
	}

	@Override
	public void run(Date now)
	{
		String buf = "";
		for (String s : messages)
		{
			if (!buf.isEmpty())
				buf += " / ";
			buf += s;
		}
		for (String email : moo.conf.getSplitEmails())
			mail.send(email, "Split", buf);
		
		eventSplit.texts = null;
	}
	
	protected LinkedList<String> messages = new LinkedList<String>();
}

class eventSplit extends event
{
	protected static textDelay texts;
	
	@Override
	public void onServerLink(server serv, server to)
	{
		boolean pypsd = serv.getName().startsWith("py") && serv.getName().endsWith(".rizon.net");
		
		if (moo.conf.getDisableSplitMessage() == false)
		{
			if (pypsd)
				for (final String channel : moo.conf.getDevChannels())
					moo.privmsg(channel, "\2" + serv.getName() + " introduced by " + to.getName() + "\2");
			else
				for (final String channel : moo.conf.getSplitChannels())
					moo.privmsg(channel, "\2" + serv.getName() + " introduced by " + to.getName() + "\2");
		}
		if (!pypsd)
		{
			if (texts == null)
			{
				texts = new textDelay();
				texts.start();
			}
			texts.messages.add(serv.getName() + " introduced by " + to.getName());
		}
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
					moo.privmsg(channel, "\2" + serv.getName() + " split from " + from.getName() + "\2 - " + serv.users + " users lost");
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
		
		if (!pypsd)
		{
			if (texts == null)
			{
				texts = new textDelay();
				texts.start();
			}
			texts.messages.add(serv.getName() + " split from " + from.getName());
		}
		
		if (moo.conf.getDisableSplitReconnect() == false && serv.isServices() == false)
		{
			reconnector r = new reconnector(serv, from);
			r.start();
		}
	}
	
	@Override
	public void onServerDestroy(server serv)
	{
		reconnector.removeReconnectsFor(serv);
	}
	
	private static final Pattern connectPattern = Pattern.compile("Remote CONNECT ([^ ]*) [0-9]* from ([^ ]*)$");
	
	@Override
	public void onWallops(final String source, final String message)
	{
		if (source.indexOf('@') != -1)
			return;
		
		Matcher m = connectPattern.matcher(message);
		if (m.find())
		{
			server s = server.findServer(m.group(1));
			if (s == null)
				return;
			
			split sp = s.getSplit();
			if (sp == null)
				return;
			
			sp.reconnectedBy = m.group(2);
		}
	}
}
