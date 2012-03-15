package net.rizon.moo.watch;

import java.util.Date;

import net.rizon.moo.moo;

public class watchEntry
{
	public enum registeredState
	{
		RS_UNKNOWN,
		RS_MANUAL,
		RS_NOT_REGISTERED,
		RS_REGISTERED
	}
	
	public String nick, creator, reason;
	public Date created, expires;
	public registeredState registered;
	private boolean requested_registered = false, warned = false;
	public boolean handled;
	
	private static String difference(Date now, Date then)
	{
		long lnow = now.getTime() / 1000L, lthen = then.getTime() / 1000L;
		
		long ldiff = now.compareTo(then) > 0 ? lnow - lthen : lthen - lnow;
		int days = 0, hours = 0, minutes = 0;
		
		if (ldiff == 0)
			return "0 seconds";
		
		while (ldiff > 86400)
		{
			++days;
			ldiff -= 86400;
		}
		while (ldiff > 3600)
		{
			++hours;
			ldiff -= 3600;
		}
		while (ldiff > 60)
		{
			++minutes;
			ldiff -= 60;
		}
		
		String buffer = "";
		if (days > 0)
			buffer += days + " day" + (days == 1 ? "" : "s") + " ";
		if (hours > 0)
			buffer += hours + " hour" + (hours == 1 ? "" : "s") + " ";
		if (minutes > 0)
			buffer += minutes + " minute" + (minutes == 1 ? "" : "s") + " ";
		if (ldiff > 0)
			buffer += ldiff + " second" + (ldiff == 1 ? "" : "s") + " ";
		buffer = buffer.trim();
		
		return buffer;
	}
	
	public void handleWatch()
	{
		this.handled = true;

		if (this.registered == watchEntry.registeredState.RS_UNKNOWN && this.requested_registered == false)
		{
			moo.privmsg("NickServ", "INFO " + this.nick);
			this.requested_registered = true;
		}
		else if (this.registered == watchEntry.registeredState.RS_NOT_REGISTERED || this.registered == watchEntry.registeredState.RS_MANUAL)
			moo.qakill(this.nick, this.reason);
		else if (this.registered == watchEntry.registeredState.RS_REGISTERED && this.warned == false)
		{
			for (final String chan : moo.conf.getOperChannels())
				moo.privmsg(chan, "PROXY: " + this.nick + " was detected on an open proxy on " + this.created + ", which was " + difference(new Date(), this.created) + " ago.");
			this.warned = true;
		}
	}
	
	public void handleOffline()
	{
		this.warned = false;
	}
}
