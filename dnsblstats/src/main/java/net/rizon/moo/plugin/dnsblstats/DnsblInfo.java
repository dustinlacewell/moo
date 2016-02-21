package net.rizon.moo.plugin.dnsblstats;

import java.util.HashMap;
import java.util.Map;

public class DnsblInfo
{
	public Map<String, Long> hits = new HashMap<>();

	public long getTotal()
	{
		long t = 0;
		for (long l : hits.values())
			t += l;
		return t;
	}
}
