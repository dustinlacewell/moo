package net.rizon.moo.plugin.proxyscan;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import io.netty.util.concurrent.ScheduledFuture;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.EventClientConnect;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.logging.LoggerUtils;
import net.rizon.moo.plugin.proxyscan.conf.ProxyscanConfiguration;
import org.slf4j.Logger;

public class proxyscan extends Plugin implements EventListener
{
	@Inject
	private static Logger logger;

	@Inject
	private ProxyStats proxystats;

	@Inject
	private Protocol protocol;

	@Inject
	private IPCache cache;

	@Inject
	private ScanListener sc;

	private ScheduledFuture cacheFuture;

	private ProxyscanConfiguration conf;
	
	private IpCycler v4Ips;
	private IpCycler v6Ips;

	public proxyscan() throws Exception
	{
		super("Proxyscan", "Checks connecting users for proxies");
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `proxies` (protocol, port, ip, date timestamp default current_timestamp)");
		
		conf = ProxyscanConfiguration.load();
		
		v4Ips = new IpCycler(conf.bindip);
		v6Ips = new IpCycler(conf.bindip6);
	}

	public ProxyscanConfiguration getConf()
	{
		return conf;
	}

	@Override
	public void start() throws Exception
	{
		sc.start();
		cacheFuture = Moo.scheduleWithFixedDelay(cache, 1, TimeUnit.SECONDS);
	}

	@Override
	public void stop()
	{
		sc.shutdown();
		cacheFuture.cancel(false);
	}

	public void akill(String ip, int port, String type, boolean input)
	{
		CacheEntry entry = cache.hit(ip);
		
		if (entry == null)
			return;

		String message = conf.ban_message.replace("%i", ip).replace("%p", "" + port).replace("%t", type);

		protocol.privmsgAll(conf.channels, "PROXY FOUND: " +
			entry.getClient().getNick() + "!" + entry.getClient().getIdent() +
			"@" + entry.getClient().getIp() + " [" + entry.getClient().getGecos() + "] " + 
			"type " + ip + ":" + port + " " + type + " (from input: " + input + ")");
		protocol.akill(ip, "+3d", message);

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
			protocol.privmsg("py-opers", "~dnsbl_admin.add " + ip + " 1 " + message);
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
		String ident = evt.getIdent();
		String ip = evt.getIp();
		String realname = evt.getRealname();
		
		logger.debug("Client connecting from {}", ip);

		if (isReserved(ip) || cache.isCached(ip))
			return;

		if (!nickOk(nick))
			return;
		
		Client client = new Client(nick, ident, ip, realname);

		logger.debug("Scanning {}", ip);

		String source = ip.contains(":") ? v6Ips.getIp() : v4Ips.getIp();
		if (source == null)
			return;

		if (conf.scan_notice != null)
		{
			String notice = conf.scan_notice.replace("%bindip%", source);
			if (!notice.isEmpty())
				protocol.notice(nick, notice);
		}

		scan(source, client);
	}

	private void scan(String sourceIp, Client client)
	{
		String path = conf.path;
		if (path.isEmpty() == true)
			return;

		File proxycheck = new File(path);
		if (proxycheck.exists() == false || proxycheck.isFile() == false || proxycheck.canExecute() == false)
			return;
		
		cache.addClient(client);

		Connector t = new Connector(sourceIp, client, this);
		LoggerUtils.initThread(Connector.logger, t);
		t.start();
	}

	@Subscribe
	public void onReload(OnReload evt)
	{
		try
		{
			conf = ProxyscanConfiguration.load();
			
			v4Ips = new IpCycler(conf.bindip);
			v6Ips = new IpCycler(conf.bindip6);
		}
		catch (Exception ex)
		{
			evt.getSource().reply("Error reloading proxyscan configuration: " + ex.getMessage());
			
			logger.warn("Unable to reload configuration", ex);
		}
	}

	@Override
	public List<Command> getCommands()
	{
		return Arrays.<Command>asList(proxystats);
	}

	@Override
	protected void configure()
	{
		bind(proxyscan.class).toInstance(this);
		
		bind(ScanListener.class);
		bind(IPCache.class);

		bind(ProxyscanConfiguration.class).toInstance(conf);

		Multibinder<EventListener> eventListenerBinder = Multibinder.newSetBinder(binder(), EventListener.class);
		eventListenerBinder.addBinding().toInstance(this);

		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder(), Command.class);
		commandBinder.addBinding().to(ProxyStats.class);
	}
}
