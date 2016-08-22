package net.rizon.moo.plugin.proxyscan;

import java.util.Date;

final class CacheEntry
{
	private final Client client;
	public final long added;
	public boolean hit;
	private int expiry;

	public CacheEntry(Client client, int expiry)
	{
		this.client = client;
		this.added = System.currentTimeMillis();
		this.expiry = expiry;
	}

	public boolean isExpired(Date now)
	{
		return new Date(this.added + (expiry * 1000L)).before(now);
	}

	public Client getClient()
	{
		return client;
	}
}
