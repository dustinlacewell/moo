package net.rizon.moo.random;

import java.util.Date;

import net.rizon.moo.event;
import net.rizon.moo.moo;
import net.rizon.moo.server;

class eventRandom extends event
{
	@Override
	protected void initDatabases()
	{
		moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `akills` (`date` DATE DEFAULT CURRENT_TIMESTAMP, `ip`, `count`)");
		moo.db.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS `akills_ip_idx` on `akills` (`ip`)");
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
			for (int i = 0; i < moo.conf.getFloodChannels().length; ++i)
				moo.privmsg(moo.conf.getFloodChannels()[i], "Removed IP " + ip + " from akill history.");
	}

	@Override
	public void onClientConnect(final String nick, final String ident, final String ip, final String real)
	{
		Date then = new Date(System.currentTimeMillis() - (30 * 1000)); // 30 seconds ago
		if (server.last_link != null && server.last_link.after(then))
			return;
		else if (server.last_split != null && server.last_split.after(then))
			return;
		
		if (ident.equals("qwebirc") || ident.equals("cgiirc") || real.equals("http://www.mibbit.com") || real.equals("...")
				|| nick.startsWith("bRO-") || real.equals("realname") || real.equals("New Now Know How") || ident.endsWith("chatzilla"))
			return;
			
		nickData nd = new nickData(nick, ident, real, ip);
		random.addNickData(nd);
		
		if (ip.indexOf('.') != -1)
			for (final String dnsbl : moo.conf.getDnsbls())
				new dnsblChecker(dnsbl, nd).start();
	}
}
