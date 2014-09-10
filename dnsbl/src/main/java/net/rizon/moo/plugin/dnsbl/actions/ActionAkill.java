package net.rizon.moo.plugin.dnsbl.actions;

import net.rizon.moo.Moo;
import net.rizon.moo.plugin.dnsbl.Blacklist;

public class ActionAkill extends Action
{
	public ActionAkill()
	{
		super("AKILL", "Ban IP and terminate connection");
	}

	@Override
	public void onHit(Blacklist blacklist, String dnsblResponse, String user)
	{
		String message = Moo.conf.getString("dnsbl.actions.akill.message")
			.replace("%h", user)
			.replace("%d", blacklist.getName())
			.replace("%r", dnsblResponse);
		Moo.akill(user, Moo.conf.getString("dnsbl.actions.akill.duration"), message);
	}

	@Override
	public boolean isUnique()
	{
		// Akills can only be done once.
		return true;
	}
}
