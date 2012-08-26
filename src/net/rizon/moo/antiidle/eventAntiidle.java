package net.rizon.moo.antiidle;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import net.rizon.moo.event;
import net.rizon.moo.moo;
import net.rizon.moo.timer;

class eventAntiIdle extends event
{
	private static final ArrayList<antiIdleVoicer> tobevoiced = new ArrayList<antiIdleVoicer>();
	
	class antiIdleVoicer extends timer
	{
		private final antiIdleEntry ai;
	
		private antiIdleVoicer(antiIdleEntry ai)
		{
			super(5, false);
			this.ai = ai;
		}
	
		@Override
		public void run(Date now)
		{
			moo.sock.write("USERHOST " + ai.nick);
		}
	}
	
	@Override
	public void onJoin(final String source, final String channel) 
	{
		if (moo.conf.getAntiIdleChannel().equalsIgnoreCase(channel) == false || moo.conf.getNick().equals(source))
			return;
		
		antiIdleEntry ai = new antiIdleEntry(source);
		antiIdleVoicer av = new antiIdleVoicer(ai);
		tobevoiced.add(av);
		ai.start();
		av.start();
	}
	
	@Override
	public void onPart(final String source, final String channel)
	{
		if (moo.conf.getAntiIdleChannel().equalsIgnoreCase(channel) == false || moo.conf.getNick().equals(source))
			return;
		
		antiIdleEntry.removeTimerFor(source);
	}

	@Override
	public void onKick(final String source, final String target, final String channel)
	{
		if (moo.conf.getAntiIdleChannel().equalsIgnoreCase(channel) == false || moo.conf.getNick().equals(target))
			return;
		
		antiIdleEntry.removeTimerFor(target);
	}
	
	@Override
	public void onMode(final String source, final String channel, final String modes)
	{
		if (moo.conf.getAntiIdleChannel().equalsIgnoreCase(channel) == false)
			return;
		
		Iterator<antiIdleVoicer> i;
		for (final String s : modes.split(" "))
		{
			antiIdleEntry.removeTimerFor(s);
			i = tobevoiced.iterator();
			
			while (i.hasNext())
			{
				antiIdleVoicer av = i.next();
				if (av.ai.nick.equals(s))
				{
					av.stop();
					i.remove();
					break;
				}
			}
		}
	}
	
	@Override
	public void onPrivmsg(final String source, final String channel, final String[] message)
	{
		if (moo.conf.getAntiIdleChannel().equalsIgnoreCase(channel) == false)
			return;
		
		antiIdleEntry.removeTimerFor(source);
	}
	
	@Override
	public void onNick(final String source, final String dest)
	{
		antiIdleEntry.renameTimerFor(source, dest);
	}
	
	@Override
	public void onQuit(final String source, final String reason)
	{
		antiIdleEntry.removeTimerFor(source);
	}
}