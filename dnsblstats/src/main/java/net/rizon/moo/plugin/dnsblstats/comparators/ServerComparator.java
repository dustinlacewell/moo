package net.rizon.moo.plugin.dnsblstats.comparators;

import com.google.inject.Inject;
import java.util.Comparator;
import net.rizon.moo.irc.Server;
import net.rizon.moo.plugin.dnsblstats.DnsblInfo;
import net.rizon.moo.plugin.dnsblstats.dnsblstats;

public class ServerComparator implements Comparator<Server>
{
	@Inject
	private dnsblstats dnsblstats;
	
	@Override
	public int compare(Server arg0, Server arg1)
	{
		DnsblInfo i0 = dnsblstats.getDnsblInfoFor(arg0), i1 = dnsblstats.getDnsblInfoFor(arg1);
		long val0 = i0 != null ? i0.getTotal() : 0, val1 = i1 != null ? i1.getTotal() : 0;
		if (val0 < val1)
			return -1;
		else if (val0 > val1)
			return 1;
		else
			return 0;
	}
}