package net.rizon.moo.plugin.proxyscan;

import net.rizon.moo.Event;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;

import java.util.logging.Level;

class EventProxyScan extends Event
{
	private static final Logger log = Logger.getLogger(EventProxyScan.class.getName());
	
	EventProxyScan()
	{
		proxyscan.cache.start();
	}
	
	
	@Override
	public void remove()
	{
		super.remove();
		proxyscan.cache.stop();
	}
	
	private static boolean isReserved(String ip)
	{
		return ip.startsWith("10.") || ip.startsWith("127.") || ip.startsWith("172.16.") || ip.startsWith("192.168.") || ip.equals("255.255.255.255");
	}

	private static boolean nickOk(String nick)
	{
		return !nick.startsWith("[EWG]");
	}
	
	private int curIp;

	@Override
	public void onClientConnect(final String nick, final String ident, final String ip, final String realname)
	{
		log.log(Level.FINE, "Client connection from " + ip);
		
		// We only scan IPv4
		if (ip.indexOf('.') == -1 || isReserved(ip) || proxyscan.cache.isCached(ip))
			return;

		if (!nickOk(nick))
			return;
		
		log.log(Level.FINE, "Scanning " + ip);
		
		String[] ips = Moo.conf.getList("proxyscan.bindip");
		if (ips.length == 0)
			return;
		
		if (curIp >= ips.length)
			curIp = 0;
		String source = ips[curIp++]; 
		
		String notice = Moo.conf.getString("proxyscan.scan_notice").replace("%bindip%", source);
		if (!notice.isEmpty())
			Moo.notice(nick, notice);
		
		proxyscan.cache.addCacheEntry(ip);
		Connector.connect(source, ip);
	}
}
