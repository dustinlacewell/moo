package net.rizon.moo.plugin.dnsbl.actions;

import com.google.inject.Inject;
import net.rizon.moo.Moo;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.plugin.dnsbl.Blacklist;

public class ActionLog extends Action
{
	@Inject
	private Config conf;

	@Inject
	private Protocol protocol;

	public ActionLog()
	{
		super("LOG", "Log connection for inspection by ircops");
	}

	@Override
	public void onHit(Blacklist blacklist, String response, String nick, String ip)
	{
		protocol.privmsgAll(conf.spam_channels, "DNSBL " + blacklist.getName() + " gave hit on " + nick + " (" + ip + "): " + response);
	}
}
