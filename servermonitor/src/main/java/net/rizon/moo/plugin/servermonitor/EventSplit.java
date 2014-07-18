package net.rizon.moo.plugin.servermonitor;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.Event;
import net.rizon.moo.Mail;
import net.rizon.moo.Moo;
import net.rizon.moo.Server;
import net.rizon.moo.Split;
import net.rizon.moo.Timer;

class textDelay extends Timer
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
		for (String email : Moo.conf.getList("split_email"))
			Mail.send(email, "Split", buf);
		
		EventSplit.texts = null;
	}
	
	protected LinkedList<String> messages = new LinkedList<String>();
}

class EventSplit extends Event
{
	protected static textDelay texts;
	
	@Override
	public void onServerLink(Server serv, Server to)
	{
		boolean pypsd = serv.getName().startsWith("py") && serv.getName().endsWith(".rizon.net");
		
		if (Moo.conf.getBool("disable_split_message") == false)
		{
			if (pypsd)
				for (final String channel : Moo.conf.getList("dev_channels"))
					Moo.privmsg(channel, "\2" + serv.getName() + " introduced by " + to.getName() + "\2");
			else
				for (final String channel : Moo.conf.getList("split_channels"))
					Moo.privmsg(channel, "\2" + serv.getName() + " introduced by " + to.getName() + "\2");
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
	public void onServerSplit(Server serv, Server from)
	{
		boolean pypsd = serv.getName().startsWith("py") && serv.getName().endsWith(".rizon.net");
		
		if (Moo.conf.getBool("disable_split_message") == false)
		{
			if (pypsd)
				for (final String channel : Moo.conf.getList("dev_channels"))
				{
					final String insult = pypsdMockery[rand.nextInt(pypsdMockery.length)];
					Moo.privmsg(channel, "\2" + serv.getName() + " split from " + from.getName() + "\2. " + insult);
				}
			else
			{
				for (final String channel : Moo.conf.getList("split_channels"))
					Moo.privmsg(channel, "\2" + serv.getName() + " split from " + from.getName() + "\2 - " + serv.users + " users lost");
				for (Server s : Server.getServers())
				{
					if (s.isHub() == true && s.getSplit() == null)
						for (Iterator<String> it2 = s.clines.iterator(); it2.hasNext();)
						{
							String cline = it2.next();
							
							if (serv.getName().equalsIgnoreCase(cline))
								for (final String channel : Moo.conf.getList("split_channels"))
									Moo.privmsg(channel, serv.getName() + " can connect to " + s.getName());
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
		
		if (Moo.conf.getBool("disable_split_reconnect") == false && serv.isServices() == false)
		{
			Reconnector r = new Reconnector(serv, from);
			r.start();
		}
	}
	
	@Override
	public void onServerDestroy(Server serv)
	{
		Reconnector.removeReconnectsFor(serv);
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
			Server s = Server.findServer(m.group(1));
			if (s == null)
				return;
			
			Split sp = s.getSplit();
			if (sp == null)
				return;
			
			sp.reconnectedBy = m.group(2);
		}
	}
}
