package net.rizon.moo.plugin.mxbl;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.NamingException;
import net.rizon.moo.Event;
import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.plugin.mxbl.dns.NS;
import net.rizon.moo.plugin.mxbl.dns.RecordType;

/**
 *
 * @author Orillion <orillion@rizon.net>
 */
public class EventRegister extends Event
{
	private final static Logger log = Logger.getLogger(EventRegister.class.getName());
	// "%s: '%s' registered by %s@%s (e-mail: %s)", s_NickServ, u->nick, u->username, u->host, (email ? email : "none")
	private final String nickServRegex = "NickServ";
	// Assumes nicks are valid (else they can't connect to the server anyway)
	private final String nicknameRegex = "[^\\u0020]+?";
	// In case of coloured username.
	private final String usernameRegex = "[^\\u0020]+?";
	// In case of coloured host for whatever reason.
	private final String hostnameRegex = "[^\\u0020]+?";
	private final String emailHostRegex = "(?:(?:[A-z0-9]|[A-z0-9][A-z0-9\\-]*[A-z0-9])\\.)*(?:[A-z0-9]|[A-z0-9][A-z0-9\\-]*[A-z0-9])";
	private final String registerRegex = "(" + nickServRegex + "): '(" + nicknameRegex + ")' registered by ("
			+ usernameRegex + ")@(" + hostnameRegex + ") \\(e-mail: [^\\s]+?@(" + emailHostRegex + ")\\)";

	private final Pattern p = Pattern.compile(registerRegex);
	private final List<RecordType> MX_RECORDS = new ArrayList<RecordType>();
	private final List<RecordType> IP_RECORDS = new ArrayList<RecordType>();

	public EventRegister()
	{
		MX_RECORDS.add(RecordType.MX);
		IP_RECORDS.add(RecordType.A);
		IP_RECORDS.add(RecordType.AAAA);
	}

	@Override
	public void onPrivmsg(final String source, final String channel, final String message)
	{
		boolean isLogChannel = true;
		if (channel == null)
		{
			return;
		}

		// Message must come from services, to prevent trolling.
		if (!source.equals("NickServ"))
		{
			return;
		}

		for (String s : Moo.conf.log_channels)
		{
			if (channel.equalsIgnoreCase(s))
			{
				isLogChannel = true;
				break;
			}
		}

		// Message is not from a services log channel.
		if (!isLogChannel)
		{
			return;
		}

		Matcher m = p.matcher(message);

		// Message does not match a nickname register message.
		if (!m.matches())
		{
			return;
		}

		String nickname = m.group(2);
		String mailhost = m.group(5);

		// Probably obsolete check.
		if (nickname == null || mailhost == null)
		{
			return;
		}

		if (!mailHostOkay(mailhost))
		{
			suspendNick(nickname);
			return;
		}

		try
		{
			HashMap<RecordType, List<String>> map = NS.lookup(mailhost, MX_RECORDS);
			List<String> list;
			if (map == null)
			{
				return;
			}
			else
			{
				list = map.get(RecordType.MX);
			}

			if (list != null && !mailHostsOkay(list))
			{
				suspendNick(nickname);
			}
		}
		catch (NamingException ex)
		{
			log.log(ex);
		}
		catch (UnknownHostException ex)
		{
			log.log(ex);
		}

	}

	/**
	 * Send NickServ the message to suspend the specified nickname.
	 * <p>
	 * @param nickname Nick to suspend.
	 */
	private void suspendNick(String nickname)
	{
		Moo.privmsgAll(Moo.conf.moo_log_channels, "Suspended nick [" + nickname + "] because it was registered with a blacklisted mailhost");
		Moo.privmsg("NickServ", "SUSPEND " + nickname + " Registered using blacklisted mailhost");
	}

	/**
	 * Checks the mailhost against a blacklisted set.
	 * <p>
	 * @param mailhost The name of the mailhost to check.
	 * <p>
	 * @return <code>true</code> if mailhost is okay, <code>false</code> if the
	 *         mailhost is blocked.
	 */
	private boolean mailHostOkay(String mailhost)
	{
		return Mailhost.getMailhost(mailhost) == null;
	}

	/**
	 * Checks the list of hosts associated with an MX Record.
	 * <p>
	 * @param hosts list of hosts to check.
	 * <p>
	 * @return <code>true</code> if mailhosts are okay, <code>false</code> if
	 *         any of the mailhosts are blocked.
	 */
	private boolean mailHostsOkay(List<String> hosts) throws NamingException, UnknownHostException
	{
		List<String> list = new ArrayList<String>();
		for (String s : hosts)
		{
			// MX Record returns "10 mx.host.com" for example, where 10 is the priority.
			String host = s.split(" ")[1];
			HashMap<RecordType, List<String>> map = NS.lookup(host, IP_RECORDS);
			if (map == null)
			{
				continue;
			}
			List<String> l;
			l = map.get(RecordType.A);
			if (l != null)
			{
				list.addAll(l);
			}
			l = map.get(RecordType.AAAA);
			if (l != null)
			{
				list.addAll(l);
			}
		}

		return ipsOkay(list);
	}

	/**
	 * Checks the list of IPs, if any of them is blocked.
	 * <p>
	 * @param ips List of IPs to check.
	 * <p>
	 * @return <code>true</code> if ips are okay, <code>false</code> if any of
	 *         the ips are blocked.
	 */
	private boolean ipsOkay(List<String> ips)
	{
		for (String s : ips)
		{
			if (MailIP.isInList(s))
			{
				return false;
			}
		}
		return true;
	}

}
