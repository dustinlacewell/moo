package net.rizon.moo.plugin.antiidle;

import io.netty.util.concurrent.ScheduledFuture;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.plugin.antiidle.conf.AntiIdleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EventAntiidle extends Event
{
	private static final Logger logger = LoggerFactory.getLogger(EventAntiidle.class);
	
	protected static final List<Voicer> toBeVoiced = new ArrayList<Voicer>();
	
	@Override
	public void onJoin(final String source, final String channel)
	{
		if (Moo.conf.general.nick.equals(source) || !antiidle.conf.channel.equalsIgnoreCase(channel))
			return;

		AntiIdleEntry ai = new AntiIdleEntry(source);
		Voicer av = new Voicer(ai);
		
		toBeVoiced.add(av);
		
		ScheduledFuture future = Moo.schedule(av, 5, TimeUnit.SECONDS);
		av.future = future;
		
		Moo.schedule(ai, antiidle.conf.time, TimeUnit.MINUTES);
	}

	@Override
	public void onPart(final String source, final String channel)
	{
		if (Moo.conf.general.nick.equals(source) || !antiidle.conf.channel.equalsIgnoreCase(channel))
			return;

		AntiIdleEntry.removeTimerFor(source);
	}

	@Override
	public void onKick(final String source, final String target, final String channel)
	{
		if (Moo.conf.general.nick.equals(source) || !antiidle.conf.channel.equalsIgnoreCase(channel))
			return;

		AntiIdleEntry.removeTimerFor(target);
	}

	@Override
	public void onMode(final String source, final String channel, final String modes)
	{
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

	@Override
	public void onPrivmsg(final String source, final String channel, final String message)
	{
		if (!antiidle.conf.channel.equalsIgnoreCase(channel))
			return;

		AntiIdleEntry.removeTimerFor(source);
	}

	@Override
	public void onNick(final String source, final String dest)
	{
		AntiIdleEntry.renameTimerFor(source, dest);
	}

	@Override
	public void onQuit(final String source, final String reason)
	{
		AntiIdleEntry.removeTimerFor(source);
	}

	/**
	 * Reloads the Configuration of antiidle.
	 * @param source Origin of the target that the !RELOAD command originated from.
	 */
	@Override
	public void onReload(CommandSource source)
	{
		try
		{
			antiidle.conf = AntiIdleConfiguration.load();
		}
		catch (Exception ex)
		{
			source.reply("Error reloading antiidle configuration: " + ex.getMessage());
			
			logger.warn("Unable to reload antiidle configuration", ex);
		}
	}
}