package net.rizon.moo.plugin.servermonitor;

import net.rizon.moo.plugin.servermonitor.server.CommandServer;
import com.google.common.eventbus.Subscribe;
import com.google.inject.multibindings.Multibinder;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Split;
import net.rizon.moo.conf.Config;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.events.EventWallops;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.events.OnServerDestroy;
import net.rizon.moo.events.OnServerLink;
import net.rizon.moo.events.OnServerSplit;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;
import net.rizon.moo.plugin.servermonitor.conf.ServerMonitorConfiguration;
import org.slf4j.Logger;

class Requester implements Runnable
{
	@Override
	public void run()
	{
		DNSChecker checker = new DNSChecker();
		Moo.injector.injectMembers(checker);
		checker.start();

		CertChecker cc = new CertChecker();
		Moo.injector.injectMembers(cc);
		checker.start();
	}
}

public class servermonitor extends Plugin implements EventListener
{
	@Inject
	private static Logger logger;
	
	@Inject
	private CommandScheck scheck;
	
	@Inject
	private CommandServer server;
	
	@Inject
	private CommandSplit split;
	
	@Inject
	private Protocol protocol;
	
	@Inject
	private Config config;
	
	@Inject
	private ServerManager serverManager;
	
	private ScheduledFuture requester;
	
	private ServerMonitorConfiguration conf;
	
	protected TextDelay texts;

	public servermonitor() throws Exception
	{
		super("Server Monitor", "Monitor servers");
		conf = ServerMonitorConfiguration.load();
	}

	@Override
	public void start() throws Exception
	{
		requester = Moo.scheduleWithFixedDelay(new Requester(), 5, TimeUnit.MINUTES);
	}

	@Override
	public void stop()
	{
		requester.cancel(false);
	}
	
	@Subscribe
	public void onServerLink(OnServerLink evt)
	{
		Server serv = evt.getServer(), to = evt.getTo();
		
		boolean pypsd = serv.getName().startsWith("py") && serv.getName().endsWith(".rizon.net");

		if (conf.messages)
		{
			if (pypsd)
				protocol.privmsgAll(config.dev_channels, "\2" + serv.getName() + " introduced by " + to.getName() + "\2");
			else
				protocol.privmsgAll(config.split_channels, "\2" + serv.getName() + " introduced by " + to.getName() + "\2");
		}
		if (!pypsd)
		{
			if (texts == null)
			{
				texts = new TextDelay();
				Moo.injector.injectMembers(texts);
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

		if (conf.messages)
		{
			if (pypsd)
				protocol.privmsgAll(config.dev_channels, "\2" + serv.getName() + " split from " + from.getName() + "\2. " + pypsdMockery[rand.nextInt(pypsdMockery.length)]);
			else
			{
				protocol.privmsgAll(config.split_channels, "\2" + serv.getName() + " split from " + from.getName() + "\2 - " + serv.users + " users lost");
				for (Server s : serverManager.getServers())
				{
					if (s.isHub() == true && s.getSplit() == null)
						for (String cline : s.clines)
							if (serv.getName().equalsIgnoreCase(cline))
								protocol.privmsgAll(config.split_channels, serv.getName() + " can connect to " + s.getName());
				}
			}
		}

		if (!pypsd)
		{
			if (texts == null)
			{
				texts = new TextDelay();
				Moo.injector.injectMembers(texts);
				Moo.schedule(texts, TextDelay.delay, TimeUnit.SECONDS);
			}
			texts.messages.add(serv.getName() + " split from " + from.getName());
		}

		if (conf.reconnect && !serv.isServices())
		{
			Reconnector r = new Reconnector(serv, from);
			Moo.injector.injectMembers(r);
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
			Server s = serverManager.findServer(m.group(1));
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
			conf = ServerMonitorConfiguration.load();
		}
		catch (Exception ex)
		{
			evt.getSource().reply("Error reloading servermonitor configuration: " + ex.getMessage());
			
			logger.warn("Unable to reload configuration", ex);
		}
	}

	@Override
	public List<Command> getCommands()
	{
		return Arrays.asList(scheck, server, split);
	}

	@Override
	protected void configure()
	{
		bind(servermonitor.class).toInstance(this);
		
		Multibinder<EventListener> eventListenerBinder = Multibinder.newSetBinder(binder(), EventListener.class);
		eventListenerBinder.addBinding().toInstance(this);

		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder(), Command.class);
		commandBinder.addBinding().to(CommandScheck.class);
		commandBinder.addBinding().to(CommandServer.class);
		commandBinder.addBinding().to(CommandSplit.class);
	}
}
