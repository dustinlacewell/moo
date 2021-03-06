package net.rizon.moo.plugin.mxbl;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.NamingException;
import net.rizon.moo.Event;
import net.rizon.moo.conf.Config;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.events.EventPrivmsg;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.plugin.mxbl.dns.NS;
import net.rizon.moo.plugin.mxbl.dns.RecordType;
import org.slf4j.Logger;

/**
 *
 * @author Orillion <orillion@rizon.net>
 */
public class EventRegister extends Event implements EventListener
{
	@Inject
	private static Logger logger;
	
	@Inject
	private Config conf;
	
	@Inject
	private Protocol protocol;

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
	private final List<RecordType> MX_RECORDS = new ArrayList<>();
	private final List<RecordType> IP_RECORDS = new ArrayList<>();

	public EventRegister()
	{
		MX_RECORDS.add(RecordType.MX);
		IP_RECORDS.add(RecordType.A);
		IP_RECORDS.add(RecordType.AAAA);
	}

	@Subscribe
	public void onPrivmsg(EventPrivmsg evt)
	{
		boolean isLogChannel = false;

		if (evt.getChannel() == null)
		{
			return;
		}

		for (String s : conf.log_channels)
		{
			if (evt.getChannel().equalsIgnoreCase(s))
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

		Matcher m = p.matcher(evt.getMessage());

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
			suspendNick(nickname, mailhost);
			return;
		}

		try
		{
			HashMap<RecordType, List<String>> map = NS.lookup(mailhost, MX_RECORDS);
			List<String> list = map != null ? map.get(RecordType.MX) : null; // list of mx hostnames

			if (list != null && (!mailHostsOkay(list) || wildcardMatch(mailhost, list)))
			{
				suspendNick(nickname, mailhost);
			}
		}
		catch (NamingException ex)
		{
			logger.info("Naming Exception: ", ex.getMessage());
		}
		catch (UnknownHostException ex)
		{
			logger.info("Unknown Host Exception: ", ex.getMessage());
		}
	}

	private boolean wildcardMatch(String mailhost, List<String> list) throws NamingException, UnknownHostException
	{
		Collection<Mailhost> wildcards = Mailhost.getMailhosts();
		for (Mailhost mw : wildcards)
		{
			if (mw.isWildcard() && StringCompare.wildcardCompare(mw.mailhost, mailhost))
			{
				Mailhost m = new Mailhost(mailhost, null, false, mw);
				for (String ip : getIPs(list))
				{
					m.addIP(ip);
				}
				m.insert();
				return true;
			}
		}

		return false;
	}

	/**
	 * Send NickServ the message to suspend the specified nickname.
	 * <p>
	 * @param nickname Nick to suspend.
	 */
	private void suspendNick(String nickname, String mailhost)
	{
		protocol.privmsgAll(conf.spam_channels, "Suspended nick [" + nickname + "] because it was registered with a blacklisted mailhost " + mailhost);
		protocol.privmsg("NickServ", "SUSPEND " + nickname + " Registered using blacklisted mailhost");
	}

	/**
	 * Checks the mailhost against a blacklisted set.
	 * <p>
	 * @param mailhost The name of the mailhost to check.
	 * <p>
	 * @return <code>true</code> if mailhost is okay, <code>false</code> if
	 *         the mailhost is blocked.
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
	 * @return <code>true</code> if mailhosts are okay, <code>false</code>
	 *         if any of the mailhosts are blocked.
	 */
	private boolean mailHostsOkay(List<String> hosts) throws NamingException, UnknownHostException
	{
		return ipsOkay(getIPs(hosts));
	}

	private List<String> getIPs(List<String> hosts) throws NamingException, UnknownHostException
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

		return list;
	}

	/**
	 * Checks the list of IPs, if any of them is blocked.
	 * <p>
	 * @param ips List of IPs to check.
	 * <p>
	 * @return <code>true</code> if ips are okay, <code>false</code> if any
	 *         of the ips are blocked.
	 */
	private boolean ipsOkay(List<String> ips)
	{
		for (String s : ips)
		{
			if (MailIP.getIP(s.trim()) != null)
			{
				return false;
			}
		}
		return true;
	}

}
