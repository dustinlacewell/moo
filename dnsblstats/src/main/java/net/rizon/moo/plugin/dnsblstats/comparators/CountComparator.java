package net.rizon.moo.plugin.dnsblstats.comparators;

import java.util.Comparator;
import java.util.Map;

public class CountComparator implements Comparator<String>
{
	public static Map<String, Long> counts = null;

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