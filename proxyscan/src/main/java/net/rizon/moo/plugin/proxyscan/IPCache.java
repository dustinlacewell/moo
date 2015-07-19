package net.rizon.moo.plugin.proxyscan;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

final class IPCache implements Runnable
{
	/*
	 * Using two caches: "cache" is a hashmap for fast lookup; cacheq is a
	 * queue for faster expiry by only looping over the items that are likely to
	 * have expired.
	 */
	private final Deque<CacheEntry> cacheq = new ArrayDeque<CacheEntry>();
	private final Map<String, CacheEntry> cache = new HashMap<String, CacheEntry>();

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

			cache.remove(e.ip);
			it.remove();
		}
	}

	public synchronized void addCacheEntry(final String ip)
	{
		CacheEntry e = new CacheEntry(ip);
		this.cache.put(ip, e);
		this.cacheq.addLast(e);
	}

	public synchronized boolean isCached(final String ip)
	{
		return this.cache.get(ip) != null;
	}

	public synchronized boolean hit(String ip)
	{
		CacheEntry e = this.cache.get(ip);
		if (e != null)
		{
			boolean save = e.hit;
			e.hit = true;
			return save;
		}
		return false;
	}
}
