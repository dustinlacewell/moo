package net.rizon.moo.plugin.random;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.util.Date;
import java.util.EventListener;
import java.util.Iterator;

import net.rizon.moo.Moo;
import net.rizon.moo.conf.Config;
import net.rizon.moo.events.EventAkillAdd;
import net.rizon.moo.events.EventAkillDel;
import net.rizon.moo.events.EventClientConnect;
import net.rizon.moo.events.EventOPMHit;
import net.rizon.moo.events.InitDatabases;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;

class EventRandom implements EventListener
{
	@Inject
	private EventBus eventBus;

	@Inject
	private Protocol protocol;

	@Inject
	private Config conf;

	@Inject
	private ServerManager serverManager;

	@Inject
	private random random;

	@Subscribe
	public void initDatabases(InitDatabases evt)
	{
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `akills` (`date` DATE DEFAULT CURRENT_TIMESTAMP, `ip`, `count`)");
		Moo.db.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS `akills_ip_idx` on `akills` (`ip`)");
	}

	private static final String opmMatch = "Using or hosting open proxies is not permitted";

	@Subscribe
	public void onAkillAdd(EventAkillAdd evt)
	{
		String reason = evt.getReason();
		String ip = evt.getIp();
		
		if (reason.contains(opmMatch))
		{
			for (Iterator<NickData> it = random.getNicks().iterator(); it.hasNext();)
			{
				NickData nd = it.next();

				if (ip.equals(nd.ip))
				{
					eventBus.post(new EventOPMHit(nd.nick_str, ip, reason));
				}
			}
			return;
		}

		if (reason.contains("open proxies") || reason.contains("open proxy"))
			return;

		random.akill(ip);
	}

	@Subscribe
	public void onAkillDel(EventAkillDel evt)
	{
		String ip = evt.getIp();
		
		if (random.remove(ip))
			protocol.privmsgAll(conf.flood_channels, "Removed IP " + ip + " from akill history.");
	}

	@Subscribe
	public void onClientConnect(EventClientConnect evt)
	{
		String ident = evt.getIdent(),
			real = evt.getRealname(),
			nick = evt.getNick(),
			ip = evt.getIp();
		
		Date then = new Date(System.currentTimeMillis() - (30 * 1000)); // 30 seconds ago
		if (serverManager.last_link != null && serverManager.last_link.after(then))
			return;
		else if (serverManager.last_split != null && serverManager.last_split.after(then))
			return;

		if (ident.equals("qwebirc") || ident.equals("cgiirc") || real.equals("http://www.mibbit.com") || real.equals("...")
				|| nick.startsWith("bRO-") || real.equals("realname") || real.equals("New Now Know How") || ident.endsWith("chatzilla")
				|| nick.startsWith("[EWG]"))
			return;

		NickData nd = new NickData(random, nick, ident, real, ip);
		random.addNickData(nd);
	}

//	@Override
//	public void onDNSBLHit(final String nick, final String ip, final String dnsbl, final String reason)
//	{
//		/* XXX this is called from a thread */
//		/*
//		NickData nickData = null;
//		for (NickData nd : random.getNicks())
//			if (!nd.dead && !nd.akilled && nd.nick_str.equals(nick))
//				nickData = nd;
//
//		if (nickData == null)
//			return;
//
//		DNSBL d = DNSBL.getList(dnsbl);
//		nickData.addList(d);
//		*/
//	}
}
