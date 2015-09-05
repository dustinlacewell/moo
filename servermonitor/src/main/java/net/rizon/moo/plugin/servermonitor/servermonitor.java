package net.rizon.moo.plugin.servermonitor;

import com.google.common.eventbus.Subscribe;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;
import net.rizon.moo.Split;
import net.rizon.moo.events.EventWallops;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.events.OnServerDestroy;
import net.rizon.moo.events.OnServerLink;
import net.rizon.moo.events.OnServerSplit;
import net.rizon.moo.plugin.servermonitor.conf.ServerMonitorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Requester implements Runnable
{
	@Override
	public void run()
	{
		new DNSChecker().start();

		CertChecker.run();
	}
}

public class servermonitor extends Plugin
{
	private static final Logger logger = LoggerFactory.getLogger(servermonitor.class);
	
	private Command scheck;
	private CommandServer server;
	private Command split;
	private ScheduledFuture requester;
	public static ServerMonitorConfiguration conf;
	protected static TextDelay texts;

	public servermonitor() throws Exception
	{
		super("Server Monitor", "Monitor servers");
		conf = ServerMonitorConfiguration.load();
	}

	@Override
	public void start() throws Exception
	{
		scheck = new CommandScheck(this);
		server = new CommandServer(this);
		split = new CommandSplit(this);

		Moo.getEventBus().register(this);

		requester = Moo.scheduleWithFixedDelay(new Requester(), 5, TimeUnit.MINUTES);
	}

	@Override
	public void stop()
	{
		scheck.remove();
		server.remove();
		split.remove();

		Moo.getEventBus().unregister(this);

		requester.cancel(false);
	}
	
	@Subscribe
	public void onServerLink(OnServerLink evt)
	{
		Server serv = evt.getServer(), to = evt.getTo();
		
		boolean pypsd = serv.getName().startsWith("py") && serv.getName().endsWith(".rizon.net");

		if (servermonitor.conf.messages)
		{
			if (pypsd)
				Moo.privmsgAll(Moo.conf.dev_channels, "\2" + serv.getName() + " introduced by " + to.getName() + "\2");
			else
				Moo.privmsgAll(Moo.conf.split_channels, "\2" + serv.getName() + " introduced by " + to.getName() + "\2");
		}
		if (!pypsd)
		{
			if (texts == null)
			{
				texts = new TextDelay();
				Moo.schedule(texts, TextDelay.delay, TimeUnit.SECONDS);
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

	@Subscribe
	public void onServerSplit(OnServerSplit evt)
	{
		Server serv = evt.getServer(), from = evt.getFrom();
		
		boolean pypsd = serv.getName().startsWith("py") && serv.getName().endsWith(".rizon.net");

		if (servermonitor.conf.messages)
		{
			if (pypsd)
				Moo.privmsgAll(Moo.conf.dev_channels, "\2" + serv.getName() + " split from " + from.getName() + "\2. " + pypsdMockery[rand.nextInt(pypsdMockery.length)]);
			else
			{
				Moo.privmsgAll(Moo.conf.split_channels, "\2" + serv.getName() + " split from " + from.getName() + "\2 - " + serv.users + " users lost");
				for (Server s : Server.getServers())
				{
					if (s.isHub() == true && s.getSplit() == null)
						for (Iterator<String> it2 = s.clines.iterator(); it2.hasNext();)
						{
							String cline = it2.next();

							if (serv.getName().equalsIgnoreCase(cline))
								Moo.privmsgAll(Moo.conf.split_channels, serv.getName() + " can connect to " + s.getName());
						}
				}
			}
		}

		if (!pypsd)
		{
			if (texts == null)
			{
				texts = new TextDelay();
				Moo.schedule(texts, TextDelay.delay, TimeUnit.SECONDS);
			}
			texts.messages.add(serv.getName() + " split from " + from.getName());
		}

		if (servermonitor.conf.reconnect && !serv.isServices())
		{
			Reconnector r = new Reconnector(serv, from);
			ScheduledFuture future = Moo.scheduleAtFixedRate(r, 1, TimeUnit.MINUTES);
			r.future = future;
		}
	}

	@Subscribe
	public void onServerDestroy(OnServerDestroy evt)
	{
		Reconnector.removeReconnectsFor(evt.getServer());
	}

	private static final Pattern connectPattern = Pattern.compile("Remote CONNECT ([^ ]*) [0-9]* from ([^ ]*)$");

	@Subscribe
	public void onWallops(EventWallops evt)
	{
		String source = evt.getSource(), message = evt.getMessage();
		
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

	@Subscribe
	public void onReload(OnReload evt)
	{
		try
		{
			servermonitor.conf = ServerMonitorConfiguration.load();
		}
		catch (Exception ex)
		{
			evt.getSource().reply("Error reloading servermonitor configuration: " + ex.getMessage());
			
			logger.warn("Unable to reload configuration", ex);
		}
	}
}
