package net.rizon.moo.plugin.mxbl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.mxbl.dns.NS;
import net.rizon.moo.plugin.mxbl.dns.RecordType;

/**
 *
 * @author Orillion <orillion@rizon.net>
 */
public class CommandBlacklist extends Command
{

	private final List<RecordType> MX_RECORDS = new ArrayList<>();
	private final List<RecordType> IP_RECORDS = new ArrayList<>();

	public CommandBlacklist(Plugin pkg)
	{
		super(pkg, "!MXBL", "Modify or view MX blacklist.");

		MX_RECORDS.add(RecordType.MX);
		IP_RECORDS.add(RecordType.A);
		IP_RECORDS.add(RecordType.AAAA);

		// TODO: Fill in correct channels.
		this.requiresChannel(Moo.conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: " + this.getCommandName() + " <ADD HOST>|<DEL HOST>|<LIST [-WILDCARD] [+LIMIT] [HOST]>|<IP IP>|<CHECK DOMAIN>");
		source.notice(" ");
		source.notice("Manipulate the blacklisted mailhost list.");
		source.notice("Examples:");
		source.notice(this.getCommandName() + " ADD rizon.net");
		source.notice("    Adds a mailhost to the list.");
		source.notice(this.getCommandName() + " DEL gmail.com");
		source.notice("    Deletes a mailhost to the list.");
		source.notice(this.getCommandName() + " LIST -WILDCARD +10 *.net");
		source.notice("    Searches for a mailhost in the list of blacklisted");
		source.notice("    mailhosts.");
		source.notice("    LIMIT defaults to 15.");
		source.notice(this.getCommandName() + " IP 127.0.0.1");
		source.notice("    Checks which record the IP belongs to, if any.");
		source.notice(this.getCommandName() + " CHECK hotmail.com");
		source.notice("    Checks which records block this domain, if any.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length < 2)
		{
			onHelp(source);
			return;
		}
		
		String command = params[1];

		if (params.length == 3 && (command.equalsIgnoreCase("a") || command.equalsIgnoreCase("add")))
		{
			add(source, params[2]);
		}
		else if (params.length == 3 && (command.equalsIgnoreCase("d") || command.equalsIgnoreCase("del") || command.equalsIgnoreCase("delete")))
		{
			delete(source, params[2]);
		}
		else if (params.length == 3 && (command.equalsIgnoreCase("ip")))
		{
			if (!findIp(source, params[2]))
			{
				source.reply("This IP is not blocked");
			}
		}
		else if (params.length == 3 && (command.equalsIgnoreCase("check")))
		{
			findBlock(source, params[2]);
		}
		else if (params.length >= 2 && (command.equalsIgnoreCase("l") || command.equalsIgnoreCase("list")))
		{
			// List all normal, default limit.
			if (params.length == 2)
			{
				listNormal(source, 15, null);
			}
			// List all wildcards, default limit.
			else if (params.length == 3 && (params[2].equalsIgnoreCase("-W") || params[2].equalsIgnoreCase("-WILDCARD")))
			{
				listWildcards(source, 15, null);
			}
			// List all normal, with limit.
			else if (params.length == 3 && params[2].startsWith("+"))
			{
				String s = params[2].substring(1);
				listWithLimit(source, s, null, false);
			}
			// List search wilcard, default limit.
			else if (params.length == 4 && (params[2].equalsIgnoreCase("-W") || params[2].equalsIgnoreCase("-WILDCARD")))
			{
				listWildcards(source, 15, params[3]);
			}
			// List search normal, with limit.
			else if (params.length == 4 && params[2].startsWith("+"))
			{
				String s = params[2].substring(1);
				listWithLimit(source, s, params[3], false);
			}
			// List all wildcard, with limit.
			else if (params.length == 4 && (params[2].equalsIgnoreCase("-W") || params[2].equalsIgnoreCase("-WILDCARD")) && params[3].startsWith("+"))
			{
				listWildcards(source, 15, null);
			}
			// List search wildcard, with limit.
			else if (params.length == 5 && (params[2].equalsIgnoreCase("-W") || params[2].equalsIgnoreCase("-WILDCARD")) && params[3].startsWith("+"))
			{
				String s = params[3].substring(1);
				listWithLimit(source, s, params[4], true);
			}
			// List search normal, default limit
			else if (params.length == 3)
			{
				listNormal(source, 15, params[2]);
			}
		}
		else
		{
			onHelp(source);
		}
	}

	private void listWithLimit(CommandSource source, String limit, String mailhost, boolean wildcard)
	{
		try
		{
			int l = Integer.parseInt(limit);
			if (wildcard)
			{
				listWildcards(source, l, mailhost);
			}
			else
			{
				listNormal(source, l, mailhost);
			}
		}
		catch (NumberFormatException ex)
		{
			source.reply(limit + " is not a valid number");
		}
	}

	private void add(CommandSource source, String mailhost)
	{
		if (mailhost.contains("*") || mailhost.contains("?"))
		{
			addWildcard(source, mailhost);
		}
		else
		{
			addNormal(source, mailhost, null);
		}
	}

	private void addWildcard(CommandSource source, String mailhost)
	{
		// Anti Kyouka-sama
		// TODO: Need to improve checks on this, since ** is allowed.
		if (mailhost.equals("*"))
		{
			source.notice("Stop trying to break it.");
			return;
		}

		Mailhost mw = Mailhost.getMailhost(mailhost);

		if (mw != null)
		{
			source.reply("\2" + mailhost + "\2 already exists, will update all matched hosts instead...");
			for (Mailhost m : mw.getChilds())
			{
				addNormal(source, m.mailhost, mw);
			}
			return;
		}

		mw = new Mailhost(mailhost, source.getUser().getNick(), true, null);
		source.reply("Added " + mailhost + " to wildcard pattern match.");
		mw.insert();
	}

	private void addNormal(CommandSource source, String mailhost, Mailhost mw)
	{
		Mailhost m = null;
		for (Mailhost ma : Mailhost.getMailhosts())
		{
			if (ma.mailhost.equalsIgnoreCase(mailhost))
			{
				m = ma;
				break;
			}
		}
		boolean updating = false;
		String oldOper = source.getUser().getNick();
		Date oldCreated = new Date();

		if (m != null)
		{
			oldOper = m.oper;
			oldCreated = m.created;
			if (mw == null)
			{
				source.reply("\2" + mailhost + "\2 already exists, will update records instead...");
			}
			else
			{
				source.reply("Updating record for \2" + mailhost + "\2.");
			}
			m.unblock();
			updating = true;
		}
		else if (mw == null)
		{
			source.reply("Attempting to look up MX records for: \2" + mailhost + "\2");
		}

		HashMap<RecordType, List<String>> l;

		l = NS.lookup(mailhost, MX_RECORDS);

		if (l == null)
		{
			if (updating)
			{
				source.reply("\2" + mailhost + "\2 no longer exists, will delete.");
			}
			else if (mw == null)
			{
				source.reply("\2" + mailhost + "\2 is not a valid host.");
			}
			return;
		}

		if (l.isEmpty())
		{
			if (updating)
			{
				source.reply("\2" + mailhost + "\2 no longer has an MX record, will delete.");
			}
			else if (mw == null)
			{
				source.reply("\2" + mailhost + "\2 does not contain any MX records.");
			}
			return;
		}

		if (mw == null)
		{
			m = new Mailhost(mailhost, oldOper, oldCreated, false, null);
		}
		else
		{
			m = new Mailhost(mailhost, null, true, mw);
		}

		for (String o : l.get(RecordType.MX))
		{
			String[] s = o.split(" ");
			HashMap<RecordType, List<String>> ipList = NS.lookup(s[1], IP_RECORDS);

			for (String a : ipList.get(RecordType.A))
			{
				m.addIP(a);
			}

			for (String a : ipList.get(RecordType.AAAA))
			{
				m.addIP(a);
			}
		}

		if (!updating)
		{
			int total = l.get(RecordType.MX).size();
			source.reply("Found " + total + " host" + (total == 1 ? "" : "s") + " in the MX record for \2" + mailhost + "\2");
		}
		else if (mw == null)
		{
			source.reply("Done updating \2" + mailhost + "\2.");
		}

		m.insert();
	}

	/**
	 * Deletes a mailhost if one can be found.
	 * <p>
	 * @param source   User issuing the command.
	 * @param mailhost Mailhost to delete.
	 */
	private void delete(CommandSource source, String mailhost)
	{
		Mailhost m = Mailhost.getMailhost(mailhost);
		if (m != null)
		{
			if (m.getOwner() == null)
			{
				m.unblock();
				source.reply("Deleted \2" + mailhost + "\2 and all associated IPs.");
			}
			else
			{
				source.reply("\2" + mailhost + "\2 belongs to \2" + m.getOwner().mailhost + "\2, please delete that entry instead.");
			}
		}
		else
		{
			source.reply("\2" + mailhost + "\2 not found.");
		}
	}

	private void listWildcards(CommandSource source, int limit, String arg)
	{
		int count = 0;
		Collection<Mailhost> mailhosts = Mailhost.getMailhosts();

		if (mailhosts.isEmpty())
		{
			source.reply("No wildcard mailhosts in database.");
			return;
		}

		for (Mailhost mw : mailhosts)
		{
			if (!mw.isWildcard())
			{
				continue;
			}
			if (arg == null)
			{
				source.reply(mw.toString());
			}
			else if (StringCompare.wildcardCompare(arg, mw.mailhost))
			{
				source.reply(mw.toString());
				for (Mailhost m : mw.getChilds())
				{
					source.reply("|-- " + m.mailhost);
				}
			}
			count++;
			if (count >= limit)
			{
				source.reply("Done showing (" + count + ") results.");
				return;
			}
		}
		source.reply("Done showing (" + count + ") results.");
	}

	private void listNormal(CommandSource source, int limit, String arg)
	{
		int count = 0;
		Collection<Mailhost> mailhosts = Mailhost.getMailhosts();
		if (mailhosts.isEmpty())
		{
			source.reply("No mailhosts blocked.");
			return;
		}
		for (Mailhost mailhost : mailhosts)
		{
			if (mailhost.isWildcard())
			{
				continue;
			}
			if (arg == null)
			{
				source.reply(mailhost.toString());
			}
			else if (StringCompare.wildcardCompare(arg, mailhost.mailhost))
			{
				source.reply(mailhost.toString());
				for (MailIP ip : mailhost.getIps())
				{
					source.reply("|-- " + ip.ip);
				}
			}
			count++;
			if (count >= limit)
			{
				source.reply("Done showing (" + count + ") results.");
				return;
			}
		}
		source.reply("Done showing (" + count + ") results.");
	}

	private boolean findIp(CommandSource source, String ip)
	{
		MailIP mailIP = MailIP.getIP(ip);
		if (mailIP != null)
		{
			source.reply(ip + " blocked by " + mailIP.getOwner().toString());
			return true;
		}
		return false;

	}

	private void findBlock(CommandSource source, String domain)
	{
		HashMap<RecordType, List<String>> map = NS.lookup(domain, MX_RECORDS);
		if (map == null)
		{
			source.reply("Domain has no MX records.");
			return;
		}

		List<String> hosts = map.get(RecordType.MX);
		boolean blocked = false;
		for (String s : hosts)
		{
			// MX Record returns "10 mx.host.com" for example, where 10 is the priority.
			String host = s.split(" ")[1];
			HashMap<RecordType, List<String>> m = NS.lookup(host, IP_RECORDS);
			if (m == null)
			{
				continue;
			}
			List<String> l;
			l = m.get(RecordType.A);
			if (l != null)
			{
				for (String ip : l)
				{
					if (findIp(source, ip))
					{
						blocked = true;
					}
				}
			}
			l = m.get(RecordType.AAAA);
			if (l != null)
			{
				for (String ip : l)
				{
					if (findIp(source, ip))
					{
						blocked = true;
					}
				}
			}
		}
		if (!blocked)
		{
			source.reply("\2" + domain + "\2 not blocked by anything.");
		}
	}
}
