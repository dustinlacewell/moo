package net.rizon.moo.plugin.dnsbl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.rizon.moo.plugin.dnsbl.conf.CacheConfiguration;

class ResultCache
{
	private static final long EXPIRE_TICK = 60 * 1000L;
	private long lastExpired;
	private int entryLifetime = 60;

	protected class Entry
	{
		private List<DnsblCheckResult> results;
		private Date expiration;

		public Entry(List<DnsblCheckResult> results, Date expiration)
		{
			this.results = results;
			this.expiration = expiration;
		}

		public List<DnsblCheckResult> getResults()
		{
			return this.results;
		}

		public boolean hasExpired()
		{
			Date now = new Date();
			return now.after(this.expiration);
		}
	}

	private Map<String, Entry> entries = new HashMap<String, Entry>();

	/**
	 * Loads the configuration for the ResultCache.
	 * @param c Configuration settings to load.
	 */
	public void load(CacheConfiguration c)
	{
		if (c.lifetime > 0)
			this.entryLifetime = c.lifetime;
	}

	public synchronized void addEntry(String ip, List<DnsblCheckResult> results)
	{
		if (System.currentTimeMillis() > lastExpired + EXPIRE_TICK)
		{
			flush();
			lastExpired = System.currentTimeMillis();
		}

		Date now = new Date();

		// Calculate expiration date.
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.SECOND, this.entryLifetime);

		Date expiration = c.getTime();
		this.entries.put(ip, new Entry(results, expiration));
	}

	public synchronized Entry hasEntry(String ip)
	{
		Entry e = this.entries.get(ip);
		if (e != null && e.hasExpired())
		{
			this.entries.remove(ip);
			return null;
		}

		return e;
	}

	public List<DnsblCheckResult> getEntry(String ip)
	{
		Entry e = this.hasEntry(ip);
		if (e == null)
			return null;

		return e.getResults();
	}

	private synchronized void flush()
	{
		for (Iterator<Map.Entry<String, Entry>> it = entries.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry<String, Entry> e = it.next();

			if (e.getValue().hasExpired())
				it.remove();
		}
	}

	public void clear()
	{
		this.entries = new HashMap<String, Entry>();
	}
}
