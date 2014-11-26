package net.rizon.moo.plugin.antiidle;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;

import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Timer;
import net.rizon.moo.plugin.antiidle.conf.AntiIdleConfiguration;

class eventAntiIdle extends Event
{
	protected static final ArrayList<antiIdleVoicer> toBeVoiced = new ArrayList<antiIdleVoicer>();

	class antiIdleVoicer extends Timer
	{
		private final AntiIdleEntry ai;

		private antiIdleVoicer(AntiIdleEntry ai)
		{
			super(5, false);
			this.ai = ai;
		}

		@Override
		public void run(Date now)
		{
			toBeVoiced.remove(this);
			Moo.sock.write("USERHOST " + ai.nick);
		}
	}

	@Override
	public void onJoin(final String source, final String channel)
	{
		if (Moo.conf.general.nick.equals(source) || !antiidle.conf.channel.equalsIgnoreCase(channel))
			return;

		AntiIdleEntry ai = new AntiIdleEntry(source);
		antiIdleVoicer av = new antiIdleVoicer(ai);
		toBeVoiced.add(av);
		ai.start();
		av.start();
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

			for (Iterator<antiIdleVoicer> it = toBeVoiced.iterator(); it.hasNext();)
			{
				antiIdleVoicer av = it.next();
				if (av.ai.nick.equals(s))
				{
					av.stop();
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
			antiidle.log.log(Level.WARNING, "Unable to reload antiidle configuration", ex);
		}
	}
}