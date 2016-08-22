package net.rizon.moo.plugin.proxyscan;

public class IpCycler
{
	private final String[] ips;
	private int curIp;

	public IpCycler(String[] ips)
	{
		this.ips = ips;

		if (ips.length == 0)
		{
			throw new IllegalArgumentException();
		}
	}

	public String getIp()
	{
		if (curIp >= ips.length)
		{
			curIp = 0;
		}
		return ips[curIp++];
	}
}
