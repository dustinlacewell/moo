package net.rizon.moo.plugin.proxyscan;

import com.google.inject.Inject;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.rizon.moo.plugin.proxyscan.conf.ProxyscanConfiguration;

final class IPCache implements Runnable
{
	/*
	 * Using two caches: "cache" is a hashmap for fast lookup; cacheq is a
	 * queue for faster expiry by only looping over the items that are likely to
	 * have expired.
	 */
	private final Deque<CacheEntry> cacheq = new ArrayDeque<>();
	private final Map<String, CacheEntry> cache = new HashMap<>();

	@Inject
	private ProxyscanConfiguration conf;

	@Override
	public synchronized void run()
	{
		Date now = new Date();
		for (Iterator<CacheEntry> it = cacheq.iterator(); it.hasNext();)
		{
			CacheEntry e = it.next();

			// 16 seconds old
			if (!e.isExpired(now))
				/*
				 * If we reach this point, all other items in the queue will
				 * be newer, too
				 */
				break;

			cache.remove(e.getClient().getIp());
			it.remove();
		}
	}
	
	public synchronized void addClient(Client client)
	{
		String ip = client.getIp();
		CacheEntry entry = new CacheEntry(client, conf.getExpiry());
		
		this.cache.put(ip, entry);
		this.cacheq.addLast(entry);
	}

	public synchronized boolean isCached(final String ip)
	{
		return this.cache.get(ip) != null;
	}

	public synchronized CacheEntry hit(String ip)
	{
		CacheEntry e = this.cache.get(ip);
		
		if (e != null && e.hit == false)
		{
			e.hit = true;
			return e;
		}
		
		return null;
	}
}
