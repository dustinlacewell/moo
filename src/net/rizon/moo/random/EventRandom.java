package net.rizon.moo.random;

import java.util.Date;

import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Server;

class EventRandom extends Event
{
	@Override
	protected void initDatabases()
	{
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `akills` (`date` DATE DEFAULT CURRENT_TIMESTAMP, `ip`, `count`)");
		Moo.db.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS `akills_ip_idx` on `akills` (`ip`)");
	}
	
	@Override
	public void onAkillAdd(final String setter, final String ip, final String reason)
	{
		if (reason.contains("hopm") || reason.contains("open proxies") || reason.contains("open proxy"))
			return;
		
		random.akill(ip);
	}
	
	@Override
	public void onAkillDel(final String setter, final String ip, final String reason)
	{
		if (random.remove(ip))
			for (String s : Moo.conf.getList("flood_channels"))
				Moo.privmsg(s, "Removed IP " + ip + " from akill history.");
	}

	@Override
	public void onClientConnect(final String nick, final String ident, final String ip, final String real)
	{
		Date then = new Date(System.currentTimeMillis() - (30 * 1000)); // 30 seconds ago
		if (Server.last_link != null && Server.last_link.after(then))
			return;
		else if (Server.last_split != null && Server.last_split.after(then))
			return;
		
		if (ident.equals("qwebirc") || ident.equals("cgiirc") || real.equals("http://www.mibbit.com") || real.equals("...")
				|| nick.startsWith("bRO-") || real.equals("realname") || real.equals("New Now Know How") || ident.endsWith("chatzilla"))
			return;
			
		NickData nd = new NickData(nick, ident, real, ip);
		random.addNickData(nd);
		
		if (ip.indexOf('.') != -1)
			for (final String dnsbl : Moo.conf.getList("random.dnsbl"))
				new DNSBLChecker(dnsbl, nd).start();
	}
}
