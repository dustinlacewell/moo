package net.rizon.moo.plugin.proxyscan;

import java.util.Date;

final class CacheEntry
{
	public final String ip;
	public final long added;
	public boolean hit;
	private int expiry;

	public CacheEntry(String ip, int expiry)
	{
		this.ip = ip;
		this.added = System.currentTimeMillis();
		this.expiry = expiry;
	}

	public boolean isExpired(Date now)
	{
		return new Date(this.added + (expiry * 1000L)).before(now);
	}
}
