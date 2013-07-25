package net.rizon.moo.watch;

import java.util.Date;

import net.rizon.moo.moo;

class watchEntry
{
	public enum registeredState
	{
		RS_UNKNOWN,
		RS_MANUAL_AKILL,
		RS_MANUAL_CAPTURE,
		RS_NOT_REGISTERED,
		RS_REGISTERED
	}
	
	public String nick, creator, reason;
	public Date created, expires;
	public registeredState registered;
	private boolean requested_registered = false, warned = false;
	public boolean handled;

	public void handleWatch()
	{
		this.handled = true;

		if (this.registered == registeredState.RS_UNKNOWN && this.requested_registered == false)
		{
			moo.privmsg("NickServ", "INFO " + this.nick);
			this.requested_registered = true;
		}
		else if (this.registered == registeredState.RS_NOT_REGISTERED || this.registered == registeredState.RS_MANUAL_AKILL)
			moo.qakill(this.nick, this.reason);
		else if (this.registered == registeredState.RS_MANUAL_CAPTURE && this.warned == false)
		{
			moo.capture(this.nick);
			this.warned = true;
		}
		else if (this.registered == registeredState.RS_REGISTERED && this.warned == false)
		{
			for (final String chan : moo.conf.getSpamChannels())
				moo.privmsg(chan, "PROXY: " + this.nick + " was detected on an open proxy on " + this.created + ", which was " + moo.difference(new Date(), this.created) + " ago.");
			this.warned = true;
		}
	}
	
	public void handleOffline()
	{
		this.warned = false;
	}
}
