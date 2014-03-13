package net.rizon.moo.plugin.antiidle;

import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;

import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.Timer;

class AntiIdleEntry extends Timer
{
	private static final Logger log = Logger.getLogger(AntiIdleEntry.class.getName());
	
	public String nick;
	public String mask;
	
	public AntiIdleEntry(final String mask)
	{
		super(Moo.conf.getInt("antiidle.time") * 60, false);
		
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
		if (Moo.conf.getInt("antiidle.bantime") > 0)
			new AntiIdleUnbanner(this.mask).start();
		Moo.kick(this.nick, Moo.conf.getString("antiidle.channel"), "You may not idle in this channel for more than " + Moo.conf.getString("antiidle.time") + " minutes.");
		
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