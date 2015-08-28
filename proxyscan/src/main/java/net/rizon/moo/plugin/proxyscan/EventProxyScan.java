package net.rizon.moo.plugin.proxyscan;

import java.util.logging.Level;

import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.plugin.proxyscan.conf.ProxyscanConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EventProxyScan extends Event
{
	private static final Logger logger = LoggerFactory.getLogger(EventProxyScan.class);

	private static boolean isReserved(String ip)
	{
		return ip.startsWith("10.") ||
				ip.startsWith("127.") ||
				ip.startsWith("172.16.") ||
				ip.startsWith("192.168.") ||
				ip.equals("255.255.255.255") ||
				ip.equals("::") ||
				ip.equals("::1");
	}

	private static boolean nickOk(String nick)
	{
		return !nick.startsWith("[EWG]");
	}

	private int curIp;

	@Override
	public void onClientConnect(final String nick, final String ident, final String ip, final String realname)
	{
		logger.debug("Client connecting from {}", ip);

		if (isReserved(ip) || proxyscan.cache.isCached(ip))
			return;

		if (!nickOk(nick))
			return;

		logger.debug("Scanning {}", ip);

		String[] ips = ip.contains(":") ? proxyscan.conf.bindip6 : proxyscan.conf.bindip;
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
			
			logger.warn("Unable to reload configuration", ex);
		}
	}
}
