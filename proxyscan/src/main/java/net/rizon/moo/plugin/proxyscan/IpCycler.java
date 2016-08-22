package net.rizon.moo.plugin.proxyscan;

public class IpCycler
{
	private final String[] ips;
	private int curIp;

	public IpCycler(String[] ips)
	{
		this.ips = ips;
	}

	public String getIp()
	{
		if (ips == null || ips.length == 0)
		{
			return null;
		}

		if (curIp >= ips.length)
		{
			curIp = 0;
		}
		return ips[curIp++];
	}
}
