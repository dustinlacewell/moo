package net.rizon.moo.plugin.antiidle;

import com.google.common.eventbus.Subscribe;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.EventJoin;
import net.rizon.moo.events.EventKick;
import net.rizon.moo.events.EventMode;
import net.rizon.moo.events.EventNickChange;
import net.rizon.moo.events.EventPart;
import net.rizon.moo.events.EventPrivmsg;
import net.rizon.moo.events.EventQuit;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.plugin.antiidle.conf.AntiIdleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class antiidle extends Plugin
{
	private static final Logger logger = LoggerFactory.getLogger(antiidle.class);
	
	private Command idle;
	private Message m;
	public static AntiIdleConfiguration conf;
	protected static final List<Voicer> toBeVoiced = new ArrayList<>();

	public antiidle() throws Exception
	{
		super("Anti idle", "Prevents users from idling in channels");
		conf = AntiIdleConfiguration.load();
	}

	@Override
	public void start() throws Exception
	{
		idle = new CommandIdle(this);
		Moo.getEventBus().register(this);
		m = new MessageUserhost();
	}

	@Override
	public void stop()
	{
		idle.remove();
		Moo.getEventBus().unregister(this);
		m.remove();
	}
	
	@Subscribe
	public void onJoin(EventJoin evt)
	{
		String source = evt.getSource(), channel = evt.getChannel();
		
		if (Moo.conf.general.nick.equals(source) || !antiidle.conf.channel.equalsIgnoreCase(channel))
			return;

		AntiIdleEntry ai = new AntiIdleEntry(source);
		Voicer av = new Voicer(ai);
		
		toBeVoiced.add(av);
		
		ScheduledFuture future = Moo.schedule(av, 5, TimeUnit.SECONDS);
		av.future = future;
		
		Moo.schedule(ai, antiidle.conf.time, TimeUnit.MINUTES);
	}

	@Subscribe
	public void onPart(EventPart evt)
	{
		String source = evt.getSource(), channel = evt.getChannel();
		
		if (Moo.conf.general.nick.equals(source) || !antiidle.conf.channel.equalsIgnoreCase(channel))
			return;

		AntiIdleEntry.removeTimerFor(source);
	}

	@Subscribe
	public void onKick(EventKick evt)
	{
		String source = evt.getSource(), channel = evt.getChannel(), target = evt.getTarget();
		
		if (Moo.conf.general.nick.equals(source) || !antiidle.conf.channel.equalsIgnoreCase(channel))
			return;

		AntiIdleEntry.removeTimerFor(target);
	}

	@Subscribe
	public void onMode(EventMode evt)
	{
		String channel = evt.getChannel(), modes = evt.getModes();
		
		if (!antiidle.conf.channel.equalsIgnoreCase(channel))
			return;

		for (final String s : modes.split(" "))
		{
			AntiIdleEntry.removeTimerFor(s);

			for (Iterator<Voicer> it = toBeVoiced.iterator(); it.hasNext();)
			{
				Voicer av = it.next();
				if (av.ai.nick.equals(s))
				{
					av.future.cancel(false);
					it.remove();
					break;
				}
			}
		}
	}

	@Subscribe
	public void onPrivmsg(EventPrivmsg evt)
	{
		String channel = evt.getChannel(), source = evt.getSource();
		
		if (!antiidle.conf.channel.equalsIgnoreCase(channel))
			return;

		AntiIdleEntry.removeTimerFor(source);
	}

	@Subscribe
	public void onNick(EventNickChange evt)
	{
		String source = evt.getSource(), dest = evt.getDest();
		
		AntiIdleEntry.renameTimerFor(source, dest);
	}

	@Subscribe
	public void onQuit(EventQuit evt)
	{
		String source = evt.getReason();
			
		AntiIdleEntry.removeTimerFor(source);
	}

	/**
	 * Reloads the Configuration of antiidle.
	 */
	@Subscribe
	public void onReload(OnReload evt)
	{
		try
		{
			antiidle.conf = AntiIdleConfiguration.load();
		}
		catch (Exception ex)
		{
			evt.getSource().reply("Error reloading antiidle configuration: " + ex.getMessage());
			
			logger.warn("Unable to reload antiidle configuration", ex);
		}
	}
}