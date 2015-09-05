package net.rizon.moo.plugin.proxyscan;

import com.google.common.eventbus.Subscribe;
import io.netty.util.concurrent.ScheduledFuture;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.EventClientConnect;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.plugin.proxyscan.conf.ProxyscanConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class proxyscan extends Plugin
{
	private static final Logger logger = LoggerFactory.getLogger(proxyscan.class);

	private ScanListener sc;
	private Command c;
	private static IPCache cache = new IPCache();
	private ScheduledFuture cacheFuture;
	public static ProxyscanConfiguration conf;

	public proxyscan() throws Exception
	{
		super("Proxyscan", "Checks connecting users for proxies");
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `proxies` (protocol, port, ip, date timestamp default current_timestamp)");
		conf = ProxyscanConfiguration.load();
	}

	@Override
	public void start() throws Exception
	{
		sc = new ScanListener(conf.server.ip, conf.server.port);
		c = new ProxyStats(this);
		sc.start();
		cacheFuture = Moo.scheduleWithFixedDelay(cache, 1, TimeUnit.SECONDS);
		Moo.getEventBus().register(this);
	}

	@Override
	public void stop()
	{
		Moo.getEventBus().unregister(this);
		sc.shutdown();
		c.remove();
		cacheFuture.cancel(false);
	}

	public static void akill(String ip, int port, String type, boolean input)
	{
		if (cache.hit(ip))
			return;

		String message = conf.ban_message.replace("%i", ip).replace("%p", "" + port).replace("%t", type);

		Moo.privmsgAll(conf.channels, "PROXY FOUND: " + ip + ":" + port + " " + type + " (from input: " + input + ")");
		Moo.akill(ip, "+3d", message);

		try
		{
			PreparedStatement statement = Moo.db.prepare("INSERT INTO `proxies` (protocol, port, ip) VALUES(?, ?, ?)");
			statement.setString(1, type);
			statement.setInt(2, port);
			statement.setString(3, ip);
			Moo.db.executeUpdate(statement);
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to record proxy hit", ex);
		}

		if (conf.py_opers)
			Moo.privmsg("py-opers", "~dnsbl_admin.add " + ip + " 1 " + message);
	}
	
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

	@Subscribe
	public void onClientConnect(EventClientConnect evt)
	{
		String nick = evt.getNick();
		String ip = evt.getIp();
		
		logger.debug("Client connecting from {}", ip);

		if (isReserved(ip) || cache.isCached(ip))
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

		cache.addCacheEntry(ip);
		Connector.connect(source, ip);
	}

	@Subscribe
	public void onReload(OnReload evt)
	{
		try
		{
			proxyscan.conf = ProxyscanConfiguration.load();
		}
		catch (Exception ex)
		{
			evt.getSource().reply("Error reloading proxyscan configuration: " + ex.getMessage());
			
			logger.warn("Unable to reload configuration", ex);
		}
	}
}
