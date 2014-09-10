package net.rizon.moo.plugin.dnsbl;

import net.rizon.moo.Command;
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
		this.requiresChannel(Moo.conf.getList("admin_channels"));
		this.blacklists = blacklistManager;
		this.cache = cache;
	}

	@Override
	public void onHelp(String source)
	{
		Moo.notice(source, "Syntax: !DNSBL <SUBCOMMAND> [ARGUMENTS...]");
		Moo.notice(source, "SUBCOMMAND can be one of: ADD, DEL, LIST, ACTION, DELACTION, CHECK, CHECKACTION.");
		Moo.notice(source, "Actions take arguments as follows:");
		Moo.notice(source, "ADD <SERVER>");
		Moo.notice(source, "  Add server to DNSBL system.");
		Moo.notice(source, "DEL <SERVER>");
		Moo.notice(source, "  Remove server and associated actions from DNSBL system.");
		Moo.notice(source, "LIST [SERVER]");
		Moo.notice(source, "  List all rules. If server if given, only list rules from server.");
		Moo.notice(source, "ACTION <SERVER> <RESPONSE> <ACTION>");
		Moo.notice(source, "  Perform action when a new connection triggers a DNSBL response.");
		Moo.notice(source, "DELACTION <SERVER> <RESPONSE> <ACTION>");
		Moo.notice(source, "  Cease doing action when a new connection triggers given response.");
		Moo.notice(source, "CHECK <IP> [SERVER]");
		Moo.notice(source, "  Check IP against DNSBLs and return responses.");
		Moo.notice(source, "  If server is given, only check against server.");
		Moo.notice(source, "CHECKACTION <IP> [SERVER]");
		Moo.notice(source, "  Check IP against DNSBLs and return what actions would have been taken.");
		Moo.notice(source, "  If server is given, only check against server. ");
		Moo.notice(source, "Possible actions:");
		for (Action a : Action.getAllActions())
			Moo.notice(source, "  " + a.getName() + ": " + a.getDescription());
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length < 2)
		{
			Moo.notice(source, "No subcommand specified. See !HELP DNSBL for details.");
			return;
		}

		String action = params[1].toLowerCase();
		String[] actionParams = Arrays.copyOfRange(params, 2, params.length);

		// Determine which subcommand to call.
		if (action.equals("add"))
			this.onAddSubCommand(source, target, actionParams);
		else if (action.equals("del"))
			this.onDelSubCommand(source, target, actionParams);
		else if (action.equals("list"))
			this.onListSubCommand(source, target, actionParams);
		else if (action.equals("action"))
			this.onActionSubCommand(source, target, actionParams);
		else if (action.equals("delaction"))
			this.onDelActionSubCommand(source, target, actionParams);
		else if (action.equals("check"))
			this.onCheckSubCommand(source, target, actionParams);
		else if (action.equals("checkaction"))
			this.onCheckActionSubCommand(source, target, actionParams);
		else
			Moo.notice(source, "Invalid subcommand. See !HELP DNSBL for details.");
	}

	/**
	 * DNSBL server add command.
	 * Syntax: !DNSBL ADD <SERVER>
	 */
	public void onAddSubCommand(String source, String target, String[] params)
	{
		if (params.length < 1)
		{
			// Invalid command.
			Moo.notice(source, "Usage: ADD [SERVER]");
			return;
		}

		String host = params[0];
		Blacklist b = this.blacklists.getBlacklist(host);
		if (b != null)
		{
			Moo.reply(source, target, "DNSBL server already exists.");
			return;
		}

		b = new Blacklist(host);
		this.blacklists.addBlacklist(b);
		this.cache.clear();
		Moo.reply(source, target, "DNSBL server " + host + " added.");
	}

	/**
	 * DNSBL server remove command.
	 * Syntax: !DNSBL DEL <SERVER>
	 */
	public void onDelSubCommand(String source, String target, String[] params)
	{
		if (params.length < 1)
		{
			// Invalid command.
			Moo.notice(source, "Usage: REMOVE [SERVER]");
			return;
		}

		String host = params[0];
		Blacklist b = this.blacklists.getBlacklist(host);
		if (b == null)
		{
			Moo.reply(source, target, "DNSBL server doesn't exist.");
			return;
		}

		this.blacklists.removeBlacklist(b);
		this.cache.clear();
		Moo.reply(source, target, "DNSBL server " + b.getName() + " removed.");
	}

	/**
	 * DNSBL server list command
	 * Syntax: !DNSBL LIST [SERVER] [SERVER...]
	 */
	public void onListSubCommand(String source, String target, String[] params)
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
					Moo.notice(source, "DNSBL server " + s + " doesn't exist.");
					continue;
				}

				blacklists.add(b);
			}
		}
		else
			blacklists = this.blacklists.getBlacklists();

		if (blacklists.isEmpty())
		{
			Moo.notice(source, "No DNSBL servers active.");
			return;
		}

		for (Blacklist b : blacklists)
		{
			Moo.notice(source, "-- " + b.getName() + " --");

			List<Rule> rules = b.getRules();
			if (!rules.isEmpty())
				for (Rule r : rules)
				{
					String response = r.getResponse();
					if (response == null)
						response = Rule.RESPONSE_ANY;

					Moo.notice(source, "  " + response + ": " + r.getAction().getName());
				}
			else
				Moo.notice(source, "  <no rules>");
		}
	}

	/**
	 * DNSBL action add command.
	 * Syntax: !DNSBL ACTION <SERVER> <RESPOMSE> <ACTION>
	 */
	public void onActionSubCommand(String source, String target, String[] params)
	{
		if (params.length < 3)
		{
			// Invalid command.
			Moo.notice(source, "Usage: ACTION [SERVER] [RESPONSE] [ACTION]");
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
			Moo.notice(source, "Invalid action: " + params[2]);
			return;
		}

		// Add server in case it's not added yet, because we're cool kids.
		Blacklist b = this.blacklists.getBlacklist(host);
		if (b == null)
		{
			b = new Blacklist(host);
			this.blacklists.addBlacklist(b);
			Moo.reply(source, target, "DNSBL server " + host + " added.");
		}
		else if (b.hasRule(actualResponse, action))
		{
			Moo.notice(source, "DNSBL rule already exists.");
			return;
		}

		Rule rule = new Rule(b, actualResponse, action);
		b.addRule(rule);
		this.cache.clear();
		Moo.reply(source, target, "DNSBL rule " + b.getName() + ":" + response + " -> " + action.getName() + " added.");
	}

	/**
	 * DNSBL action remove command.
	 * Syntax: !DNSBL DELACTION <SERVER> <RESPONSE> <ACTION>
	 */
	public void onDelActionSubCommand(String source, String target, String[] params)
	{
		if (params.length < 3)
		{
			// Invalid command.
			Moo.notice(source, "Usage: DELACTION [SERVER] [RESPONSE] [ACTION]");
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
			Moo.notice(source, "Invalid action: " + params[2]);
			return;
		}

		Blacklist b = this.blacklists.getBlacklist(host);
		if (b == null)
		{
			Moo.notice(source, "Unknown DNSBL server: " + host);
			return;
		}

		Rule r = b.getRule(actualResponse, action);
		if (r == null)
		{
			Moo.notice(source, "Unknown DNSBL rule.");
			return;
		}

		b.removeRule(r);
		this.cache.clear();
		Moo.reply(source, target, "DNSBL rule " + b.getName() + ":" + response + " -> " + action.getName() + " deleted.");
	}

	/**
	 * DNSBL check command.
	 * Syntax: !DNSBL CHECK <IP> [SERVER] [SERVER...]
	 */
	public void onCheckSubCommand(String source, String target, String[] params)
	{
		if (params.length < 1)
		{
			Moo.notice(source, "Usage: CHECK <IP> [SERVER]");
			return;
		}

		DnsblCheckTarget host = DnsblCheckTarget.find(params[0]);
		if (host == null)
		{
			Moo.notice(source, "Invalid IP: " + params[0]);
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
					Moo.notice(source, "DNSBL server " + params[i] + " doesn't exist.");
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
			Moo.notice(source, "-- " + b.getName() + ":" + host.getIP() + " --");
			Record[] result = checker.getDnsblResponse(b);
			if (result == null)
				Moo.notice(source, "  <no results>");
			else
				for (Record r : result)
					Moo.notice(source, "  " + r.rdataToString());
		}
	}

	/**
	 * DNSBL check action command.
	 * Syntax: !DNSBL CHECKACTION <IP> [SERVER] [SERVER...]
	 */
	public void onCheckActionSubCommand(String source, String target, String[] params)
	{
		if (params.length < 1)
		{
			Moo.notice(source, "Usage: CHECKACTION <IP> [SERVER]");
			return;
		}

		DnsblCheckTarget host = DnsblCheckTarget.find(params[0]);
		if (host == null)
		{
			Moo.notice(source, "Invalid IP: " + params[0]);
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
					Moo.notice(source, "DNSBL server " + params[i] + " doesn't exist.");
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
			Moo.notice(source, "-- " + b.getName() + ":" + host.getIP() + " --");

			DnsblCheckResult result = checker.check(b);
			if (result == null)
				Moo.notice(source, "  <no results>");
			else
				for (Map.Entry<String, List<Action>> entry : result.getActions().entrySet())
					for (Action a : entry.getValue())
						Moo.notice(source, "  " + entry.getKey() + " -> " + a.getName());
		}
	}
}
