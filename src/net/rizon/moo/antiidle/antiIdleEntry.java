package net.rizon.moo.antiidle;

import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;

import net.rizon.moo.logger;
import net.rizon.moo.moo;
import net.rizon.moo.timer;

class antiIdleEntry extends timer
{
	private static final logger log = logger.getLogger(antiIdleEntry.class.getName());
	
	public String nick;
	public String mask;
	
	public antiIdleEntry(final String mask)
	{
		super(moo.conf.getAntiIdleTime() * 60, false);
		
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
		if (moo.conf.getAntiIdleBanTime() > 0)
			new antiIdleUnbanner(this.mask).start();
		moo.kick(this.nick, moo.conf.getAntiIdleChannel(), "You may not idle in this channel for more than " + moo.conf.getAntiIdleTime() + " minutes.");
		
		entries.remove(this.nick.toLowerCase());
	}
	
	private static HashMap<String, antiIdleEntry> entries = new HashMap<String, antiIdleEntry>();
	
	public static boolean removeTimerFor(final String mask)
	{
		String nick = mask;
		int e = mask.indexOf('!');
		if (e != -1)
			nick = mask.substring(0, e);
		
		antiIdleEntry a = entries.get(nick.toLowerCase());
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
		
		antiIdleEntry a = entries.get(nick.toLowerCase());
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