package net.rizon.moo.plugin.dnsbl.actions;

import net.rizon.moo.Moo;
import net.rizon.moo.plugin.dnsbl.Blacklist;

public class ActionLog extends Action
{
	public ActionLog()
	{
		super("LOG", "Log connection for inspection by ircops");
	}

	@Override
	public void onHit(Blacklist blacklist, String response, String nick, String ip)
	{
		Moo.privmsgAll(Moo.conf.spam_channels, "DNSBL " + blacklist.getName() + " gave hit on " + nick + " (" + ip + "): " + response);
	}
}
