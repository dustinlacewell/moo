package net.rizon.moo.plugin.antiidle;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.rizon.moo.Moo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AntiIdleEntry implements Runnable
{
	private static final Logger logger = LoggerFactory.getLogger(AntiIdleEntry.class);
	private static Map<String, AntiIdleEntry> entries = new HashMap<String, AntiIdleEntry>();

	public String nick;
	public String mask;
	private boolean defunct;

	public AntiIdleEntry(final String mask)
	{
		String nick = mask;
		int e = mask.indexOf('!');
		if (e != -1)
			nick = mask.substring(0, e);

		this.nick = nick;
		this.mask = mask;

		entries.put(this.nick.toLowerCase(), this);

		logger.debug("Adding antiidle for {}", this.nick);
	}

	@Override
	public void run()
	{
		if (defunct)
			return;
		
		if (antiidle.conf.bantime > 0)
			Moo.schedule(new AntiIdleUnbanner(this.mask), antiidle.conf.bantime, TimeUnit.MINUTES);
		antiidle.protocol.kick(this.nick, antiidle.conf.channel, "You may not idle in this channel for more than " + antiidle.conf.time + " minutes.");

		entries.remove(this.nick.toLowerCase());
	}

	public static boolean removeTimerFor(final String mask)
	{
		String nick = mask;
		int e = mask.indexOf('!');
		if (e != -1)
			nick = mask.substring(0, e);

		AntiIdleEntry a = entries.get(nick.toLowerCase());
		if (a != null)
		{
			logger.debug("Removing antiidle for {}", nick);

			a.defunct = true;
			entries.remove(nick.toLowerCase());
			return true;
		}

		return false;
	}

	public static void renameTimerFor(final String mask, final String to)
	{
		String nick = mask;
		int e = mask.indexOf('!');
		if (e != -1)
			nick = mask.substring(0, e);

		AntiIdleEntry a = entries.get(nick.toLowerCase());
		if (a != null)
		{
			entries.remove(nick.toLowerCase());

			logger.debug("Renaming antiidle for {} to {}", nick, to);

			a.nick = to;
			a.mask = mask;

			entries.put(to.toLowerCase(), a);
		}
	}
}