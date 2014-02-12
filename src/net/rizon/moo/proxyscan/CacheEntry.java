package net.rizon.moo.proxyscan;

import java.util.Date;

final class CacheEntry
{
	public final String ip;
	public final long added;
	
	public CacheEntry(final String ip)
	{
		this.ip = ip;
		this.added = System.currentTimeMillis();
	}
	
	public boolean isExpired(Date now)
	{
		return new Date(this.added + (IPCache.expiry_time * 1000L)).before(now);
	}
}
