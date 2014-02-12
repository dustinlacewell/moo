package net.rizon.moo.proxyscan;

import java.util.logging.Level;

import net.rizon.moo.Event;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;

class EventProxyScan extends Event
{
	private static final Logger log = Logger.getLogger(EventProxyScan.class.getName());
	private final IPCache cache = new IPCache();
	
	EventProxyScan()
	{
		this.cache.start();
	}
	
	
	@Override
	public void remove()
	{
		super.remove();
		this.cache.stop();
	}

	@Override
	public void onClientConnect(final String nick, final String ident, final String ip, final String realname)
	{
		log.log(Level.FINE, "Client connection from " + ip);
		
		// We only scan IPv4
		if (ip.indexOf('.') == -1 || this.cache.isCached(ip))
			return;
		
		log.log(Level.FINE, "Scanning " + ip);
		
		String notice = Moo.conf.getString("proxyscan.scan_notice");
		if (!notice.isEmpty())
			Moo.notice(nick, notice);
		
		this.cache.addCacheEntry(ip);
		Connector.connect(ip);
	}
}
