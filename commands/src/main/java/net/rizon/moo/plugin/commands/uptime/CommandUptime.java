package net.rizon.moo.plugin.commands.uptime;

import com.google.inject.Inject;
import java.util.Date;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Split;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;
import net.rizon.moo.util.Match;
import net.rizon.moo.util.TimeDifference;


class CommandUptime extends Command
{
	@Inject
	private ServerManager serverManager;

	@Inject
	private Protocol protocol;

	@Inject
	public CommandUptime(Config conf)
	{
		super("!UPTIME", "View server uptimes");

		this.requiresChannel(conf.staff_channels);
		this.requiresChannel(conf.oper_channels);
		this.requiresChannel(conf.admin_channels);
	}

	private static boolean only_extremes;
	private static String want_server;

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !UPTIME [{ ALL | server.name }]");
		source.notice("Without any parameters, the highest and lowest uptime and last split times");
		source.notice("are sought and shown.");
		source.notice("If ALL is given, uptimes and times since the last split for all servers will");
		source.notice("be shown.");
		source.notice("If a server name is given, the uptime and last split time for that particular");
		source.notice("will be shown.");
		source.notice("The lowest last split time and the highest uptime will be colored "
				+ Message.COLOR_GREEN + "green" + Message.COLOR_END + ",");
		source.notice("the highest last split time will be colored "
				+ Message.COLOR_RED + "red" + Message.COLOR_END + ".");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		want_server = null;

		if (params.length > 1)
		{
			only_extremes = false;

			if (!params[1].equalsIgnoreCase("ALL"))
			{
				want_server = params[1];
			}
		}
		else
			only_extremes = true;

		Message242.waiting_for.clear();
		for (Server s : serverManager.getServers())
		{
			if (s.isServices() == false && s.getSplit() == null)
			{
				protocol.write("STATS", "u", s.getName());
				Message242.waiting_for.add(s.getName());
			}
		}

		Message242.source = source;
	}

	private Split findLastSplit(Server s)
	{
		for (int i = s.getSplits().length; i > 0; --i)
		{
			Split sp = s.getSplits()[i - 1];

			Server serv = serverManager.findServerAbsolute(sp.from);
			if (serv == null)
				continue;

			boolean b = false;
			for (int j = serv.getSplits().length; j > 0; --j)
			{
				Split upsp = serv.getSplits()[j - 1];

				if (upsp.when.equals(sp.when))
					b = true;
			}
			if (b == true)
				continue;

			return sp;
		}

		return null;
	}

	private int dashesFor(Server s)
	{
		int longest = 0;
		for (Server s2 : serverManager.getServers())
		{
			int l = s2.getName().length();
			if (l > longest)
				longest = l;
		}

		return longest - s.getName().length() + 2;
	}

	public void post_update(CommandSource source)
	{
		Date highest = null, lowest = null;
		Split highest_sp = null, lowest_sp = null;
		Date now = new Date();

		for (Server s : serverManager.getServers())
		{
			if (s.isServices() || s.uptime == null)
				continue;

			Split sp = findLastSplit(s);

			if (highest == null || s.uptime.before(highest))
				highest = s.uptime;
			if (lowest == null || s.uptime.after(lowest))
				lowest = s.uptime;
			if (highest_sp == null || (sp != null && sp.when.before(highest_sp.when)))
				highest_sp = sp;
			if (lowest_sp == null || (sp != null && sp.when.after(lowest_sp.when)))
				lowest_sp = sp;
		}

		boolean shown = false;
		for (Server s : serverManager.getServers())
		{
			if (s.isServices() || s.uptime == null)
				continue;
			else if (want_server != null && Match.matches(s.getName(), "*" + want_server + "*") == false)
				continue;

			boolean is_extreme = false;

			Split sp = findLastSplit(s);
			int dashes = dashesFor(s);

			String buffer = "[UPTIME] " + s.getName() + " ";
			for (int i = 0; i < dashes; ++i)
				buffer += "-";
			buffer += " ";

			if (s.uptime == highest)
			{
				buffer += Message.COLOR_GREEN;
				is_extreme = true;
			}
			else if (s.uptime == lowest)
			{
				buffer += Message.COLOR_RED;
				is_extreme = true;
			}
			buffer += s.uptime.toString();
			buffer += Message.COLOR_END;

			if (sp != null)
			{
				buffer += " - ";
				if (sp == highest_sp)
				{
					buffer += Message.COLOR_GREEN;
					is_extreme = true;
				}
				else if (sp == lowest_sp)
				{
					buffer += Message.COLOR_RED;
					is_extreme = true;
				}
				buffer += TimeDifference.difference(now, sp.when);
				buffer += Message.COLOR_END;
			}

			if (CommandUptime.only_extremes)
			{
				if (is_extreme)
				{
					source.reply(buffer);
					shown = true;
				}
			}
			else
			{
				source.reply(buffer);
				shown = true;
			}
		}

		if (shown == false)
		{
			if (want_server != null)
				source.reply("No servers match " + want_server);
			else
				source.reply("You have managed to execute a command with no servers on the network, congrats");
		}
	}
}
