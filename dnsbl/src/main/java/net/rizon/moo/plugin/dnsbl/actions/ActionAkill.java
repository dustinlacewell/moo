package net.rizon.moo.plugin.dnsbl.actions;

import net.rizon.moo.Moo;
import net.rizon.moo.plugin.dnsbl.Blacklist;
import net.rizon.moo.plugin.dnsbl.dnsbl;

public class ActionAkill extends Action
{
	public ActionAkill()
	{
		super("AKILL", "Ban IP and terminate connection");
	}

	@Override
	public void onHit(Blacklist blacklist, String dnsblResponse, String nick, String ip)
	{
		String message = dnsbl.conf.akill.message
			.replace("%h", ip)
			.replace("%d", blacklist.getName())
			.replace("%r", dnsblResponse);
		Moo.akill(ip, dnsbl.conf.akill.duration, message);
	}

	@Override
	public boolean isUnique()
	{
		// Akills can only be done once.
		return true;
	}
}
