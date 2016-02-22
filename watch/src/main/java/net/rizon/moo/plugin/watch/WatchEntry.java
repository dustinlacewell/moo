package net.rizon.moo.plugin.watch;

import java.util.Date;

import net.rizon.moo.Moo;

class WatchEntry
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
	public boolean requested_registered = false, warned = false;
	public boolean handled;
}
