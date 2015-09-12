package net.rizon.moo.plugin.mxbl;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Orillion <orillion@rizon.net>
 */
public class MailIP
{
	private static final Map<String, MailIP> ips = new HashMap<String, MailIP>();
	public final String ip;
	private final Mailhost owner;

	@SuppressWarnings("LeakingThisInConstructor")
	MailIP(String ip, Mailhost owner)
	{
		this.ip = ip.trim();
		this.owner = owner;
		ips.put(ip, this);
	}

	public Mailhost getOwner()
	{
		return this.owner;
	}

	public static MailIP getIP(String ip)
	{
		return ips.get(ip.trim());
	}

	public static void deleteIP(MailIP ip)
	{
		ips.remove(ip.ip);
	}
}
