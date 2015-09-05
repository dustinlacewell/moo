package net.rizon.moo.events;

public class EventDNSBLHit
{
	private final String nick, ip, dnsbl, reuslt;

	public EventDNSBLHit(String nick, String ip, String dnsbl, String reuslt)
	{
		this.nick = nick;
		this.ip = ip;
		this.dnsbl = dnsbl;
		this.reuslt = reuslt;
	}

	public String getNick()
	{
		return nick;
	}

	public String getIp()
	{
		return ip;
	}

	public String getDnsbl()
	{
		return dnsbl;
	}

	public String getReuslt()
	{
		return reuslt;
	}
}
