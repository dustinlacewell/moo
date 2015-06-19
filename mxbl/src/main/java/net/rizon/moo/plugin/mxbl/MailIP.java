/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rizon.moo.plugin.mxbl;

import java.util.HashMap;

/**
 *
 * @author Orillion <orillion@rizon.net>
 */
public class MailIP
{
	private static final HashMap<String, MailIP> ips = new HashMap<String, MailIP>();
	public final String ip;
	private final Mailhost owner;

	@SuppressWarnings("LeakingThisInConstructor")
	MailIP(String ip, Mailhost owner)
	{
		this.ip = ip;
		this.owner = owner;
		ips.put(ip.trim(), this);
	}

	public Mailhost getOwner()
	{
		return this.owner;
	}

	public static boolean isInList(String ip)
	{
		return ips.containsKey(ip);
	}

	public static MailIP getMailIP(String ip)
	{
		return ips.get(ip);
	}

	public static void delete(MailIP ip)
	{
		ips.remove(ip.ip);
	}

}
