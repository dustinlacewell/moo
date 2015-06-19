package net.rizon.moo.plugin.mxbl;

import java.util.ArrayList;
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
	private final List<RecordType> MX_RECORDS = new ArrayList<RecordType>();
	private final List<RecordType> IP_RECORDS = new ArrayList<RecordType>();

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
		source.notice("Syntax: " + this.getCommandName() + " <ADD HOST>|<DEL HOST>|<LIST [+LIMIT] [HOST]>|<IP IP>");
		source.notice(" ");
		source.notice("Manipulate the blacklisted mailhost list.");
		source.notice("Examples:");
		source.notice(this.getCommandName() + " ADD *.rizon.net");
		source.notice("    Adds a mailhost to the list.");
		source.notice(this.getCommandName() + " DEL gmail.com");
		source.notice("    Deletes a mailhost to the list.");
		source.notice(this.getCommandName() + " LIST +10 *.net");
		source.notice("    Searches for a mailhost in the list of blacklisted");
		source.notice("    mailhosts.");
		source.notice("    LIMIT defaults to 15.");
		source.notice(this.getCommandName() + " IP 127.0.0.1");
		source.notice("    Checks which record the IP belongs to, if any.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		String command;
		if (params.length >= 2)
		{
			command = params[1];
		}
		else
		{
			onHelp(source);
			return;
		}

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
			findIp(source, params[2]);
		}
		else if (params.length >= 2 && (command.equalsIgnoreCase("l") || command.equalsIgnoreCase("list")))
		{
			if (params.length == 2)
			{
				list(source, 15, null);
			}
			else if (params.length == 3 && params[2].startsWith("+"))
			{
				String s = params[2].substring(1);
				listWithLimit(source, s, null);
			}
			else if (params.length == 3)
			{
				list(source, 15, params[2]);
			}
			else if (params.length == 4 && params[2].startsWith("+"))
			{
				String s = params[2].substring(1);
				listWithLimit(source, s, params[3]);
			}
			else if (params.length == 4 && params[3].startsWith("+"))
			{
				// Noob proofing
				String s = params[3].substring(1);
				listWithLimit(source, s, params[2]);
			}
		}
		else
		{
			onHelp(source);
		}
	}

	private void listWithLimit(CommandSource source, String limit, String mailhost)
	{
		try
		{
			int l = Integer.parseInt(limit);
			list(source, l, null);
		}
		catch (NumberFormatException ex)
		{
			source.reply(limit + " is not a valid number");
		}
	}

	private void add(CommandSource source, String mailhost)
	{
		Mailhost m = Mailhost.getMailhost(mailhost);
		boolean updating = false;

		if (m != null)
		{
			source.reply("\2" + mailhost + "\2 already exists, will update records instead...");
			m.unblock();
			updating = true;
		}
		else
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
			else
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
			else
			{
				source.reply("\2" + mailhost + "\2 does not contain any MX records.");
			}
			return;
		}

		m = new Mailhost(mailhost, source.getUser().getNick());

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
		else
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
			m.unblock();
			source.reply("Deleted \2" + mailhost + "\2 and all associated IPs.");
		}
		else
		{
			source.reply("\2" + mailhost + "\2 not found.");
		}
	}

	private void list(CommandSource source, int limit, String arg)
	{
		int count = 0;
		if (Mailhost.mailhosts.isEmpty())
		{
			source.reply("No mailhosts blocked.");
		}
		for (Mailhost mailhost : Mailhost.getMailhosts())
		{
			if (arg == null)
			{
				source.reply(mailhost.toString());
			}
			else if (StringCompare.wildcardCompare(arg, mailhost.mailhost))
			{
				source.reply(mailhost.toString());
				for (MailIP ip : mailhost.ips)
				{
					source.reply("|-- " + ip.ip);
				}
			}
			count++;
			if (count >= limit)
			{
				break;
			}
		}
	}

	private void findIp(CommandSource source, String ip)
	{
		MailIP mailIP = MailIP.getMailIP(ip);
		if (mailIP == null)
		{
			source.reply("This IP is not blocked");
		}
		else
		{
			source.reply(ip + " belongs to: " + mailIP.getOwner().toString());
		}
	}
}
