package net.rizon.moo.plugin.dnsbl;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;

class DnsblCheckTarget
{
	private InetAddress ip;
	private boolean check;

	private DnsblCheckTarget(InetAddress ip, boolean check)
	{
		this.ip = ip;
		this.check = check;
	}

	public InetAddress getIP()
	{
		return this.ip;
	}

	public boolean isIPv6()
	{
		return this.ip instanceof Inet6Address;
	}

	public boolean shouldCheck()
	{
		return this.check;
	}

	/**
	 * Get check target from IP.
	 * The result decides the final IP to check after mangling, whether it's IPv6 or not
	 * and whether it should be checked at all.
	 */
	public static DnsblCheckTarget find(String ip)
	{
		InetAddress addr;
		try
		{
			addr = InetAddress.getByName(ip);
		}
		catch (UnknownHostException e)
		{
			return null;
		}

		boolean shouldCheck =
			!addr.isAnyLocalAddress() &&
			!addr.isLinkLocalAddress() &&
			!addr.isLoopbackAddress() &&
			!addr.isMulticastAddress() &&
			!addr.isSiteLocalAddress();

		return new DnsblCheckTarget(addr, shouldCheck);
	}
}
