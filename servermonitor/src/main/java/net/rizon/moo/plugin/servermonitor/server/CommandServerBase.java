package net.rizon.moo.plugin.servermonitor.server;

import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Iterator;
import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Message;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;
import net.rizon.moo.plugin.servermonitor.conf.ServerMonitorConfiguration;
import net.rizon.moo.util.Match;

class CommandServerBase extends Command
{
	@Inject
	private ServerManager serverManager;
	
	@Inject
	private ServerMonitorConfiguration conf;
	
	public CommandServerBase(Config conf, String command)
	{
		super(command, "Views servers");

		this.requiresChannel(conf.staff_channels);
		this.requiresChannel(conf.oper_channels);
		this.requiresChannel(conf.admin_channels);
	}

	private static boolean isLink(Server s, Server targ)
	{
		for (Iterator<Server> it = s.links.iterator(); it.hasNext();)
		{
			Server s2 = it.next();

			if (targ == s2)
				return true;
		}

		return false;
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length >= 3 && params[1].equalsIgnoreCase("FREEZE"))
		{
			Server s = serverManager.findServer(params[2]);
			if (s == null)
				source.reply("No such server " + params[2]);
			else if (s.frozen == true)
				source.reply(s.getName() + " is already frozen");
			else
			{
				s.frozen = true;
				source.reply("Froze server " + s.getName());
			}
		}
		else if (params.length >= 3 && params[1].equalsIgnoreCase("UNFREEZE"))
		{
			Server s = serverManager.findServer(params[2]);
			if (s == null)
				source.reply("No such server " + params[2]);
			else if (s.frozen == false)
				source.reply(s.getName() + " is already unfrozen");
			else
			{
				s.frozen = false;
				source.reply("Unfroze server " + s.getName());
			}
		}
		else if (params.length >= 3 && (params[1].equalsIgnoreCase("DELETE") || params[1].equalsIgnoreCase("DEL")))
		{
			Server s = serverManager.findServerAbsolute(params[2]);
			if (s == null)
				source.reply("No such server " + params[2] + ", full name required");
			else
			{
				source.reply("Deleted server " + s.getName());
				serverManager.removeServer(s);
			}
		}
		else if (params.length >= 3 && params[1].equalsIgnoreCase("DESC"))
		{
			Server s = serverManager.findServer(params[2]);
			if (s == null)
				source.reply("No such server " + params[2]);
			else if (params.length == 3)
			{
				s.setDesc("");
				source.reply("Description for " + s.getName() + " unset");
			}
			else
			{
				String desc = params[3];
				for (int i = 4; i < params.length; ++i)
					desc += " " + params[i];
				s.setDesc(desc);
				source.reply("Description for " + s.getName() + " updated");
			}
		}
		else if (params.length >= 3)
		{
			Server s = serverManager.findServer(params[1]);
			if (s == null)
				source.reply("Server " + params[1] + " not found");
			else if (params[2].equals("."))
			{
				s.allowed_clines.clear();
				source.reply("Allowed links for " + s.getName() + " unset");
			}
			else
			{
				boolean modified = false;

				for (int i = 2; i < params.length; ++i)
				{
					Server arg = serverManager.findServer(params[i].substring(1));
					if (arg == null)
					{
						source.reply("No such server: " + params[i]);
						continue;
					}
					else if (arg == s)
					{
						source.reply("Servers can not link to themselves");
						continue;
					}
					else if (modified == true && s.allowed_clines.contains(arg.getName()))
					{
						source.reply(s.getName() + " already has " + arg.getName());
						continue;
					}
					else if (arg.isHub() == false)
					{
						source.reply("You may only link servers to hubs");
						continue;
					}

					if (modified == false)
					{
						s.allowed_clines.clear();
						modified = true;
					}
					s.allowed_clines.add(arg.getName());
				}

				source.reply("Prefered links for " + s.getName() + " set to " + s.allowed_clines.toString());
			}
		}
		else
		{
			boolean all = params.length > 1 && params[1].equalsIgnoreCase("ALL");
			String match = params.length > 1 && all == false ? params[1] : null;
			if (match != null)
				all = true;
			boolean all_output = false;
			for (Server s : serverManager.getServers())
			{
				boolean output = false;

				if (match != null)
				{
					if (Match.matches(s.getName(), "*" + match + "*"))
						output = true;
					else
						continue;
				}
				else if (s.isServices())
					continue;

				int diff = s.users - s.last_users;
				String change = String.valueOf(diff);
				if (diff == 0)
					change = "";
				else if (change.startsWith("-") == false)
					change = "+" + change;
				boolean bigChange = diff >= 50 || diff <= -50;

				String links = "";
				HashSet<String> why = new HashSet<String>();
				for (Iterator<String> it = s.allowed_clines.iterator(); it.hasNext();)
				{
					String link_name = it.next();
					Server link_server = serverManager.findServerAbsolute(link_name);

					if (link_server == null)
					{
						links += Message.COLOR_RED;
						output = true;
					}
					else if (link_server.getSplit() != null)
					{
						links += Message.COLOR_ORANGE;
						output = true;
					}
					else if (s.clines.contains(link_server.getName()) == false)
					{
						links += Message.COLOR_YELLOW;
						output = true;
					}
					else if (link_server.frozen)
					{
						links += Message.COLOR_BRIGHTBLUE;
						output = true;
					}
					else
						links += Message.COLOR_GREEN;

					if (link_server != null)
					{
						if (isLink(s, link_server))
							links += Message.COLOR_UNDERLINE + link_server.getName() + Message.COLOR_UNDERLINE + " (" + link_server.links.size() + ")";
						else
							links += link_server.getName() + " (" + link_server.links.size() + ")";
					}
					else
						links += link_name;
					links += Message.COLOR_END +  ", ";
				}
				if (links.isEmpty() == false)
					links = links.substring(0, links.length() - 2);
				else
					links = "N/A";

				String msg = "";
				boolean showAllCLines = false;
				if (s.getSplit() != null)
				{
					msg += Message.COLOR_RED;
					output = true;
				}
				else if (s.frozen || !conf.reconnect)
				{
					msg += Message.COLOR_BRIGHTBLUE;
					output = true;
				}
				else if (bigChange)
				{
					msg += Message.COLOR_ORANGE;
					output = true;
				}
				else if (s.allowed_clines.isEmpty() == false)
				{
					boolean good = false;
					for (Iterator<String> it = s.allowed_clines.iterator(); it.hasNext();)
					{
						Server p_s = serverManager.findServerAbsolute(it.next());
						if (p_s != null && isLink(s, p_s))
							good = true;
					}

					if (good == false)
					{
						output = true;
						showAllCLines = true;
						why.add("Uplink not an allowed server");
					}
				}
				if (s.clines.size() == 1)
				{
					output = true;
					showAllCLines = true;
					why.add("Only one CLine");
				}

				if (s.clines.size() > 0)
				{
					int frzcount = 0;
					for (String linkname : s.clines)
					{
						Server link = serverManager.findServerAbsolute(linkname);
						if (link == null)
							continue;

						if (link.frozen)
							frzcount += 1;
					}

					if (frzcount == s.clines.size())
					{
						output = true;
						showAllCLines = true;
						why.add("All CLines frozen");
					}
				}

				msg += "[Users: " + s.users + change + "] ";
				msg += s.getName();
				msg += Message.COLOR_END;
				msg += " / " + links;

				links = "";
				for (Iterator<String> it = s.clines.iterator(); it.hasNext();)
				{
					String cline_name = it.next();
					Server cline_server = serverManager.findServerAbsolute(cline_name);

					if (cline_server == null)
						links += Message.COLOR_RED;
					else if (cline_server.isServices())
						continue;
					else if (cline_server.getSplit() != null)
						links += Message.COLOR_ORANGE;
					else if (cline_server.clines.contains(s.getName()) == false)
						links += Message.COLOR_YELLOW;
					else if (cline_server.frozen)
						links += Message.COLOR_BRIGHTBLUE;
					else if (all == false && showAllCLines == false)
						continue;
					else
						links += Message.COLOR_GREEN;

					output = true;

					if (cline_server != null)
					{
						if (isLink(s, cline_server))
							links += Message.COLOR_UNDERLINE + cline_server.getName() + Message.COLOR_UNDERLINE + " (" + cline_server.links.size() + ")";
						else
							links += cline_server.getName() + " (" + cline_server.links.size() + ")";
					}
					else
						links += cline_name;
					links += Message.COLOR_END +  ", ";
				}
				if (links.isEmpty() == false)
					links = links.substring(0, links.length() - 2);
				else
					links = "N/A";

				msg += " / " + links;

				if (why.isEmpty() == false)
					msg += " / " + why.toString();

				if (s.getDesc().isEmpty() == false)
				{
					msg += " / " + s.getDesc();
					output = true;
				}

				if (output || all)
				{
					source.reply(msg);
					all_output = true;
				}
			}

			int total_diff = serverManager.cur_total_users - serverManager.last_total_users;
			String change = String.valueOf(total_diff);
			if (change.startsWith("-") == false)
				change = "+" + change;

			if (match == null && (all_output || all))
				source.reply("Total Users: " + serverManager.cur_total_users + change);
		}
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: " + this.getCommandName() + " [freeze|unfreeze|delete] server.name [.|all|preferred clines]");
		source.notice(" ");
		source.notice(this.getCommandName() + " is used to view C-Lines between servers and configure allowed C-Lines,");
		source.notice("which are C-Lines that have preference over others. When servers split the auto reconnector will");
		source.notice("use this information to determine where to reconnect servers.");
		source.notice(" ");
		source.notice("The output format of this command is as follows: users / allowed C-lines / all C-Lines");
		source.notice("If allowed C-lines is set, the server will only be auto reconnected to the given allowed servers.");
		source.notice("The number in parentheses next to C-Lines are the number of links active on that hub.");
		source.notice(" ");
		source.notice("C-Lines are color coded to determine the state of the C-Line");
		source.notice(" " + Message.COLOR_RED + "red" + Message.COLOR_END + " - Represents a C-Line to a nonexistent server");
		source.notice(" " + Message.COLOR_ORANGE + "orange" + Message.COLOR_END + " - Represents a C-Line to a split server");
		source.notice(" " + Message.COLOR_YELLOW + "yellow" + Message.COLOR_END + " - Represents a broken C-Line with only one direction (no return C-Line)");
		source.notice(" " + Message.COLOR_BRIGHTBLUE + "blue" + Message.COLOR_END + " - Represents a C-Line to a frozen server");
		source.notice(" " + Message.COLOR_GREEN + "green" + Message.COLOR_END + " - Represents a good C-Line");
		source.notice(" " + Message.COLOR_UNDERLINE + "Underlines" + Message.COLOR_UNDERLINE + " are used to show what the current uplink for a server is.");
		source.notice("Frozen servers are made using the freeze command. Frozen servers will not be connected to, but may be reconnected to the network.");
		source.notice(" ");
		source.notice("Executing this command with no parameters will only show problem servers and and the problem C-Lines on those servers.");
		source.notice("When using ALL or searching for a specific name all C-Lines will be shown.");
		source.notice("If a single dot is provided after server.name, all allowed C-Lines will be unset.");
	}
}
