package net.rizon.moo.plugin.dnsbl;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.dnsbl.actions.Action;
import org.xbill.DNS.Record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;


class CommandDnsbl extends Command
{
	private BlacklistManager blacklists;
	private ResultCache cache;

	public CommandDnsbl(Plugin pkg, BlacklistManager blacklistManager, ResultCache cache)
	{
		super(pkg, "!DNSBL", "Manage DNSBL servers");
		this.requiresChannel(Moo.conf.admin_channels);
		this.blacklists = blacklistManager;
		this.cache = cache;
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !DNSBL <SUBCOMMAND> [ARGUMENTS...]");
		source.notice("SUBCOMMAND can be one of: ADD, DEL, LIST, ACTION, DELACTION, CHECK, CHECKACTION.");
		source.notice("Actions take arguments as follows:");
		source.notice("ADD <SERVER>");
		source.notice("  Add server to DNSBL system.");
		source.notice("DEL <SERVER>");
		source.notice("  Remove server and associated actions from DNSBL system.");
		source.notice("LIST [SERVER]");
		source.notice("  List all rules. If server if given, only list rules from server.");
		source.notice("ACTION <SERVER> <RESPONSE> <ACTION>");
		source.notice("  Perform action when a new connection triggers a DNSBL response.");
		source.notice("DELACTION <SERVER> <RESPONSE> <ACTION>");
		source.notice("  Cease doing action when a new connection triggers given response.");
		source.notice("CHECK <IP> [SERVER]");
		source.notice("  Check IP against DNSBLs and return responses.");
		source.notice("  If server is given, only check against server.");
		source.notice("CHECKACTION <IP> [SERVER]");
		source.notice("  Check IP against DNSBLs and return what actions would have been taken.");
		source.notice("  If server is given, only check against server. ");
		source.notice("Possible actions:");
		for (Action a : Action.getAllActions())
			source.notice("  " + a.getName() + ": " + a.getDescription());
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length < 2)
		{
			source.reply("No subcommand specified. See !HELP DNSBL for details.");
			return;
		}

		String action = params[1].toLowerCase();
		String[] actionParams = Arrays.copyOfRange(params, 2, params.length);

		// Determine which subcommand to call.
		if (action.equals("add"))
			this.onAddSubCommand(source, actionParams);
		else if (action.equals("del"))
			this.onDelSubCommand(source, actionParams);
		else if (action.equals("list"))
			this.onListSubCommand(source, actionParams);
		else if (action.equals("action"))
			this.onActionSubCommand(source, actionParams);
		else if (action.equals("delaction"))
			this.onDelActionSubCommand(source, actionParams);
		else if (action.equals("check"))
			this.onCheckSubCommand(source, actionParams);
		else if (action.equals("checkaction"))
			this.onCheckActionSubCommand(source, actionParams);
		else
			source.reply("Invalid subcommand. See !HELP DNSBL for details.");
	}

	/**
	 * DNSBL server add command.
	 * Syntax: !DNSBL ADD <SERVER>
	 */
	public void onAddSubCommand(CommandSource source, String[] params)
	{
		if (params.length < 1)
		{
			// Invalid command.
			source.reply("Usage: ADD [SERVER]");
			return;
		}

		String host = params[0];
		Blacklist b = this.blacklists.getBlacklist(host);
		if (b != null)
		{
			source.reply("DNSBL server already exists.");
			return;
		}

		b = new Blacklist(host);
		this.blacklists.addBlacklist(b);
		this.cache.clear();
		source.reply("DNSBL server " + host + " added.");
	}

	/**
	 * DNSBL server remove command.
	 * Syntax: !DNSBL DEL <SERVER>
	 */
	public void onDelSubCommand(CommandSource source, String[] params)
	{
		if (params.length < 1)
		{
			// Invalid command.
			source.reply("Usage: REMOVE [SERVER]");
			return;
		}

		String host = params[0];
		Blacklist b = this.blacklists.getBlacklist(host);
		if (b == null)
		{
			source.reply("DNSBL server doesn't exist.");
			return;
		}

		this.blacklists.removeBlacklist(b);
		this.cache.clear();
		source.reply("DNSBL server " + b.getName() + " removed.");
	}

	/**
	 * DNSBL server list command
	 * Syntax: !DNSBL LIST [SERVER] [SERVER...]
	 */
	public void onListSubCommand(CommandSource source, String[] params)
	{
		Collection<Blacklist> blacklists;

		if (params.length > 0)
		{
			blacklists = new ArrayList<Blacklist>();

			for (String s : params)
			{
				Blacklist b = this.blacklists.getBlacklist(s);
				if (b == null)
				{
					source.reply("DNSBL server " + s + " doesn't exist.");
					continue;
				}

				blacklists.add(b);
			}
		}
		else
			blacklists = this.blacklists.getBlacklists();

		if (blacklists.isEmpty())
		{
			source.reply("No DNSBL servers active.");
			return;
		}

		for (Blacklist b : blacklists)
		{
			source.reply("-- " + b.getName() + " --");

			List<Rule> rules = b.getRules();
			if (!rules.isEmpty())
				for (Rule r : rules)
				{
					String response = r.getResponse();
					if (response == null)
						response = Rule.RESPONSE_ANY;

					source.reply("  " + response + ": " + r.getAction().getName());
				}
			else
				source.reply("  <no rules>");
		}
	}

	/**
	 * DNSBL action add command.
	 * Syntax: !DNSBL ACTION <SERVER> <RESPOMSE> <ACTION>
	 */
	public void onActionSubCommand(CommandSource source, String[] params)
	{
		if (params.length < 3)
		{
			// Invalid command.
			source.reply("Usage: ACTION [SERVER] [RESPONSE] [ACTION]");
			return;
		}

		String host = params[0];
		String response = params[1], actualResponse;
		Action action = Action.getByName(params[2].toUpperCase());

		if (response.equals(Rule.RESPONSE_ANY))
			actualResponse = null;
		else
			actualResponse = response;

		if (action == null)
		{
			source.reply("Invalid action: " + params[2]);
			return;
		}

		// Add server in case it's not added yet, because we're cool kids.
		Blacklist b = this.blacklists.getBlacklist(host);
		if (b == null)
		{
			b = new Blacklist(host);
			this.blacklists.addBlacklist(b);
			source.reply("DNSBL server " + host + " added.");
		}
		else if (b.hasRule(actualResponse, action))
		{
			source.reply("DNSBL rule already exists.");
			return;
		}

		Rule rule = new Rule(b, actualResponse, action);
		b.addRule(rule);
		this.cache.clear();
		source.reply("DNSBL rule " + b.getName() + ":" + response + " -> " + action.getName() + " added.");
	}

	/**
	 * DNSBL action remove command.
	 * Syntax: !DNSBL DELACTION <SERVER> <RESPONSE> <ACTION>
	 */
	public void onDelActionSubCommand(CommandSource source, String[] params)
	{
		if (params.length < 3)
		{
			// Invalid command.
			source.reply("Usage: DELACTION [SERVER] [RESPONSE] [ACTION]");
			return;
		}

		String host = params[0];
		String response = params[1], actualResponse;
		Action action = Action.getByName(params[2].toUpperCase());

		if (response.equals(Rule.RESPONSE_ANY))
			actualResponse = null;
		else
			actualResponse = response;

		if (action == null)
		{
			source.reply("Invalid action: " + params[2]);
			return;
		}

		Blacklist b = this.blacklists.getBlacklist(host);
		if (b == null)
		{
			source.reply("Unknown DNSBL server: " + host);
			return;
		}

		Rule r = b.getRule(actualResponse, action);
		if (r == null)
		{
			source.reply("Unknown DNSBL rule.");
			return;
		}

		b.removeRule(r);
		this.cache.clear();
		source.reply("DNSBL rule " + b.getName() + ":" + response + " -> " + action.getName() + " deleted.");
	}

	/**
	 * DNSBL check command.
	 * Syntax: !DNSBL CHECK <IP> [SERVER] [SERVER...]
	 */
	public void onCheckSubCommand(CommandSource source, String[] params)
	{
		if (params.length < 1)
		{
			source.reply("Usage: CHECK <IP> [SERVER]");
			return;
		}

		DnsblCheckTarget host = DnsblCheckTarget.find(params[0]);
		if (host == null)
		{
			source.reply("Invalid IP: " + params[0]);
			return;
		}

		Collection<Blacklist> blacklists;

		if (params.length > 1)
		{
			blacklists = new ArrayList<Blacklist>();

			for (int i = 1; i < params.length; i++)
			{
				Blacklist b = this.blacklists.getBlacklist(params[i]);
				if (b == null)
				{
					source.reply("DNSBL server " + params[i] + " doesn't exist.");
					continue;
				}

				blacklists.add(b);
			}
		}
		else
			blacklists = this.blacklists.getBlacklists();

		DnsblChecker checker = new DnsblChecker(host, this.blacklists);
		for (Blacklist b : blacklists)
		{
			source.reply("-- " + b.getName() + ":" + host.getIP() + " --");
			Record[] result = checker.getDnsblResponse(b);
			if (result == null)
				source.reply("  <no results>");
			else
				for (Record r : result)
					source.reply("  " + r.rdataToString());
		}
	}

	/**
	 * DNSBL check action command.
	 * Syntax: !DNSBL CHECKACTION <IP> [SERVER] [SERVER...]
	 */
	public void onCheckActionSubCommand(CommandSource source, String[] params)
	{
		if (params.length < 1)
		{
			source.reply("Usage: CHECKACTION <IP> [SERVER]");
			return;
		}

		DnsblCheckTarget host = DnsblCheckTarget.find(params[0]);
		if (host == null)
		{
			source.reply("Invalid IP: " + params[0]);
			return;
		}

		Collection<Blacklist> blacklists;

		if (params.length > 1)
		{
			blacklists = new ArrayList<Blacklist>();

			for (int i = 1; i < params.length; i++)
			{
				Blacklist b = this.blacklists.getBlacklist(params[i]);
				if (b == null)
				{
					source.reply("DNSBL server " + params[i] + " doesn't exist.");
					continue;
				}

				blacklists.add(b);
			}
		}
		else
			blacklists = this.blacklists.getBlacklists();

		DnsblChecker checker = new DnsblChecker(host, this.blacklists);
		for (Blacklist b : blacklists)
		{
			source.reply("-- " + b.getName() + ":" + host.getIP() + " --");

			DnsblCheckResult result = checker.check(b);
			if (result == null)
				source.reply("  <no results>");
			else
				for (Map.Entry<String, List<Action>> entry : result.getActions().entrySet())
					for (Action a : entry.getValue())
						source.reply("  " + entry.getKey() + " -> " + a.getName());
		}
	}
}
