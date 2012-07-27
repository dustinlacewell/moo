package net.rizon.moo.antiidle;

import net.rizon.moo.event;
import net.rizon.moo.moo;

class eventAntiIdle extends event
{
	@Override
	public void onJoin(final String source, final String channel) 
	{
		if (moo.conf.getAntiIdleChannel().equalsIgnoreCase(channel) == false || moo.conf.getNick().equals(source))
			return;
		
		antiIdleEntry ai = new antiIdleEntry(source);
		moo.sock.write("USERHOST " + ai.nick);
		ai.start();
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
		
		for (final String s : modes.split(" "))
			antiIdleEntry.removeTimerFor(s);
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
}