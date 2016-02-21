package net.rizon.moo.plugin.dnsbl.actions;

import com.google.inject.Inject;
import static net.rizon.moo.Moo.moo;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.plugin.dnsbl.Blacklist;
import net.rizon.moo.plugin.dnsbl.conf.DnsblConfiguration;

public class ActionAkill extends Action
{
	@Inject
	private DnsblConfiguration conf;

	@Inject
	private Protocol protocol;

	public ActionAkill()
	{
		super("AKILL", "Ban IP and terminate connection");
	}

	@Override
	public void onHit(Blacklist blacklist, String dnsblResponse, String nick, String ip)
	{
		String message = conf.akill.message
			.replace("%h", ip)
			.replace("%d", blacklist.getName())
			.replace("%r", dnsblResponse);
		protocol.akill(ip, conf.akill.duration, message);
	}

	@Override
	public boolean isUnique()
	{
		// Akills can only be done once.
		return true;
	}
}
