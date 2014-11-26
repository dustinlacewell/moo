package net.rizon.moo.plugin.antiidle;

import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.Timer;

import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;

class AntiIdleEntry extends Timer
{
	private static final Logger log = Logger.getLogger(AntiIdleEntry.class.getName());

	public String nick;
	public String mask;

	public AntiIdleEntry(final String mask)
	{
		super(antiidle.conf.time * 60, false);

		String nick = mask;
		int e = mask.indexOf('!');
		if (e != -1)
			nick = mask.substring(0, e);

		this.nick = nick;
		this.mask = mask;

		entries.put(this.nick.toLowerCase(), this);

		log.log(Level.FINER, "Adding antiidle for " + this.nick);
	}

	@Override
	public void run(Date now)
	{
		if (antiidle.conf.bantime > 0)
			new AntiIdleUnbanner(this.mask).start();
		Moo.kick(this.nick, antiidle.conf.channel, "You may not idle in this channel for more than " + antiidle.conf.time + " minutes.");

		entries.remove(this.nick.toLowerCase());
	}

	private static HashMap<String, AntiIdleEntry> entries = new HashMap<String, AntiIdleEntry>();

	public static boolean removeTimerFor(final String mask)
	{
		String nick = mask;
		int e = mask.indexOf('!');
		if (e != -1)
			nick = mask.substring(0, e);

		AntiIdleEntry a = entries.get(nick.toLowerCase());
		if (a != null)
		{
			log.log(Level.FINER, "Removing antiidle for " + nick);

			a.stop();
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

			log.log(Level.FINER, "Renaming antiidle for " + nick + " to " + to);

			a.nick = to;
			a.mask = mask;

			entries.put(to.toLowerCase(), a);
		}
	}
}