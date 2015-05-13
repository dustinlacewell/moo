package net.rizon.moo.plugin.proxyscan;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.proxyscan.conf.ProxyscanConfiguration;

public class proxyscan extends Plugin
{
	protected static final Logger log = Logger.getLogger(proxyscan.class.getName());

	private Event e;
	private ScanListener sc;
	private Command c;
	protected static final IPCache cache = new IPCache();
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
		e = new EventProxyScan();
		sc = new ScanListener(conf.server.ip, conf.server.port);
		c = new ProxyStats(this);
		sc.start();
	}

	@Override
	public void stop()
	{
		e.remove();
		sc.shutdown();
		c.remove();
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
			log.log(Level.WARNING, "Unable to record proxy hit", ex);
		}

		if (conf.py_opers)
			Moo.privmsg("py-opers", "~dnsbl_admin.add " + ip + " 1 " + message);
	}
}
