/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rizon.moo.plugin.mxbl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Orillion <orillion@rizon.net>
 */
public class MailIP
{
	private static final HashMap<Mailhost, HashSet<MailIP>> ips = new HashMap<Mailhost, HashSet<MailIP>>();
	public final String ip;
	private final Mailhost owner;

	@SuppressWarnings("LeakingThisInConstructor")
	MailIP(String ip, Mailhost owner)
	{
		this.ip = ip.trim();
		this.owner = owner;
		HashSet<MailIP> set = ips.get(this.owner);
		if (set == null)
		{
			set = new HashSet<MailIP>();
			ips.put(this.owner, set);
		}
		set.add(this);
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof MailIP)
		{
			return this.hashCode() == o.hashCode();
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		int hash = 5;
		hash = 67 * hash + (this.ip != null ? this.ip.hashCode() : 0);
		return hash;
	}

	public Mailhost getOwner()
	{
		return this.owner;
	}

	public static boolean isInList(String ip)
	{
		for (HashSet<MailIP> s : ips.values())
		{
			for (MailIP mailIP : s)
			{
				if (mailIP.ip.equals(ip.trim()))
				{
					return true;
				}
			}
		}
		return false;
	}

	public static List<MailIP> getAllMailIP(String ip)
	{
		List<MailIP> list = new ArrayList<MailIP>();
		for (HashSet<MailIP> s : ips.values())
		{
			for (MailIP mailIP : s)
			{
				if (mailIP.ip.equals(ip.trim()))
				{
					list.add(mailIP);
				}
			}
		}
		return list;
	}

	public static HashSet<MailIP> getMailIP(Mailhost owner)
	{
		return ips.get(owner);
	}

	public static void delete(MailIP ip)
	{
		ips.get(ip.getOwner()).remove(ip);
	}

	public static void delete(Mailhost m)
	{
		ips.remove(m);
	}

}
