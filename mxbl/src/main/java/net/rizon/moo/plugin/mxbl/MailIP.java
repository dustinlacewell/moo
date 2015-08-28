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
	public final String ip;
	private final Mailhost owner;

	@SuppressWarnings("LeakingThisInConstructor")
	MailIP(String ip, Mailhost owner)
	{
		this.ip = ip.trim();
		this.owner = owner;
	}

	public Mailhost getOwner()
	{
		return this.owner;
	}
}
