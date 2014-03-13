package net.rizon.moo.plugin.dnsbl;

import java.util.HashMap;

class DnsblInfo
{
	public HashMap<String, Long> hits = new HashMap<String, Long>();
	
	public long getTotal()
	{
		long t = 0;
		for (long l : hits.values())
			t += l;
		return t;
	}
}