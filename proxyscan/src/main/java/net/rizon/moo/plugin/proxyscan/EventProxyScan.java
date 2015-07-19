package net.rizon.moo.plugin.proxyscan;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.plugin.proxyscan.conf.ProxyscanConfiguration;

class EventProxyScan extends Event
{
	private static final Logger log = Logger.getLogger(EventProxyScan.class.getName());

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

		String[] ips = proxyscan.conf.bindip;
		if (ips.length == 0)
			return;

		if (curIp >= ips.length)
			curIp = 0;
		String source = ips[curIp++];

		String notice = proxyscan.conf.scan_notice.replace("%bindip%", source);
		if (!notice.isEmpty())
			Moo.notice(nick, notice);

		proxyscan.cache.addCacheEntry(ip);
		Connector.connect(source, ip);
	}

	@Override
	public void onReload(CommandSource source)
	{
		try
		{
			proxyscan.conf = ProxyscanConfiguration.load();
		}
		catch (Exception ex)
		{
			source.reply("Error reloading proxyscan configuration: " + ex.getMessage());
			proxyscan.log.log(Level.WARNING, "Unable to reload proxyscan configuration", ex);
		}
	}
}
