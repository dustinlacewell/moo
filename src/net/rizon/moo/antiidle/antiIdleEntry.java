package net.rizon.moo.antiidle;

import java.util.Date;
import java.util.HashMap;

import net.rizon.moo.moo;
import net.rizon.moo.timer;

class antiIdleEntry extends timer
{
	public String nick;
	public String mask;
	public long join;
	
	public antiIdleEntry(final String mask)
	{
		super(moo.conf.getAntiIdleTime() * 60, false);
		
		String nick = mask;
		int e = mask.indexOf('!');
		if (e != -1)
			nick = mask.substring(0, e);
		
		this.nick = nick;
		this.mask = mask;
		this.join = System.currentTimeMillis() / 1000L;
		
		entries.put(this.nick.toLowerCase(), this);
		
		if (moo.conf.getDebug() > 0)
			System.out.println("antiidle: Adding antiidle for " + this.nick);
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
			if (moo.conf.getDebug() > 0)
				System.out.println("antiidle: Removing antiidle for " + nick);
			
			a.stop();
			entries.remove(nick.toLowerCase());
			return true;
		}
		
		return false;
	}
}