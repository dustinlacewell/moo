package net.rizon.moo.plugin.dnsbl;

import java.util.Comparator;
import java.util.HashMap;

import net.rizon.moo.Server;

class dnsblServerComparator implements Comparator<Server>
{
	public static dnsblServerComparator cmp = new dnsblServerComparator();

	@Override
	public int compare(Server arg0, Server arg1)
	{
		DnsblInfo i0 = dnsbl.getDnsblInfoFor(arg0), i1 = dnsbl.getDnsblInfoFor(arg1);
		long val0 = i0 != null ? i0.getTotal() : 0, val1 = i1 != null ? i1.getTotal() : 0;
		if (val0 < val1)
			return -1;
		else if (val0 > val1)
			return 1;
		else
			return 0;
	}
}

class dnsblCountComparator implements Comparator<String>
{
	public static dnsblCountComparator cmp = new dnsblCountComparator();
	public static HashMap<String, Long> counts = null;
	
	@Override
	public int compare(String arg0, String arg1)
	{
		long val0 = counts.get(arg0), val1 = counts.get(arg1);
		if (val0 < val1)
			return -1;
		else if (val0 > val1)
			return 1;
		else
			return 0;
	}
}