package net.rizon.moo.servermonitor;

import java.util.HashSet;
import java.util.Iterator;

import net.rizon.moo.command;
import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;

class commandServerBase extends command
{
	public commandServerBase(mpackage pkg, final String command)
	{
		super(pkg, command, "Views servers");
	}
	
	private static boolean isLink(server s, server targ)
	{
		for (Iterator<String> it = s.links.iterator(); it.hasNext();)
		{
			server s2 = server.findServerAbsolute(it.next());
			
			if (targ == s2)
				return true;
		}
		
		return false;
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length >= 3 && params[1].equalsIgnoreCase("FREEZE"))
		{
			server s = server.findServer(params[2]);
			if (s == null)
				moo.reply(source, target, "No such server " + params[2]);
			else if (s.frozen == true)
				moo.reply(source, target, s.getName() + " is already frozen");
			else
			{
				s.frozen = true;
				moo.reply(source, target, "Froze server " + s.getName());
			}
		}
		else if (params.length >= 3 && params[1].equalsIgnoreCase("UNFREEZE"))
		{
			server s = server.findServer(params[2]);
			if (s == null)
				moo.reply(source, target, "No such server " + params[2]);
			else if (s.frozen == false)
				moo.reply(source, target, s.getName() + " is already unfrozen");
			else
			{
				s.frozen = false;
				moo.reply(source, target, "Unfroze server " + s.getName());
			}
		}
		else if (params.length >= 3 && params[1].equalsIgnoreCase("DELETE"))
		{
			server s = server.findServer(params[2]);
			if (s == null)
				moo.reply(source, target, "No such server " + params[2] + ", full name required");
			else
			{
				moo.reply(source, target, "Deleted server " + s.getName());
				reconnector.removeReconnectsFor(s);
				s.destroy();
			}
		}
		else if (params.length >= 3)
		{
			server s = server.findServer(params[1]);
			if (s == null)
				moo.reply(source, target, "Server " + params[1] + " not found");
			else
			{
				boolean modified = false;
				
				for (int i = 2; i < params.length; ++i)
				{
					server arg = server.findServer(params[i].substring(1));
					if (arg == null)
					{
						moo.reply(source, target, "No such server: " + params[i]);
						continue;
					}
					else if (arg == s)
					{
						moo.reply(source, target, "Servers can not link to themselves");
						continue;
					}
					else if (modified == true && s.preferred_links.contains(arg.getName()))
					{
						moo.reply(source, target, s.getName() + " already has " + arg.getName());
						continue;
					}
					else if (arg.isHub() == false)
					{
						moo.reply(source, target, "You may only link servers to hubs");
						continue;
					}
					
					if (modified == false)
					{
						s.preferred_links.clear();
						modified = true;
					}
					s.preferred_links.add(arg.getName());
				}
				
				moo.reply(source, target, "Prefered links for " + s.getName() + " set to " + s.preferred_links.toString());
			}
		}
		else
		{
			boolean all = params.length > 1 && params[1].equalsIgnoreCase("ALL");
			String match = params.length > 1 && all == false ? params[1] : null;
			if (match != null)
				all = true;
			boolean all_output = false;
			for (server s : server.getServers())
			{
				boolean output = false;
				
				if (match != null)
				{
					if (moo.match(s.getName(), "*" + match + "*"))
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
				for (Iterator<String> it = s.preferred_links.iterator(); it.hasNext();)
				{
					String link_name = it.next();
					server link_server = server.findServerAbsolute(link_name);
					
					if (link_server == null)
					{
						links += message.COLOR_RED;
						output = true;
						why.add("CLine to dead link");
					}
					else if (link_server.getSplit() != null)
					{
						links += message.COLOR_ORANGE;
						output = true;
						why.add("CLine to split link");
					}
					else if (s.clines.contains(link_server.getName()) == false)
					{
						links += message.COLOR_YELLOW;
						output = true;
						why.add("Broken CLine");
					}
					else if (link_server.frozen)
					{
						links += message.COLOR_BRIGHTBLUE;
						output = true;
					}
					else
						links += message.COLOR_GREEN;
					
					if (link_server != null)
					{
						if (isLink(s, link_server))
							links += message.COLOR_UNDERLINE + link_server.getName() + message.COLOR_UNDERLINE + " (" + link_server.links.size() + ")";
						else
							links += link_server.getName() + " (" + link_server.links.size() + ")";
					}
					else
						links += link_name;
					links += message.COLOR_END +  ", ";
				}
				if (links.isEmpty() == false)
					links = links.substring(0, links.length() - 2);
				else
					links = "N/A";
				
				String msg = "";
				if (s.getSplit() != null)
				{
					msg += message.COLOR_RED;
					output = true;
					why.add("Split server");
				}
				else if (s.frozen || moo.conf.getDisableSplitReconnect())
				{
					msg += message.COLOR_BRIGHTBLUE;
					output = true;
					why.add("Frozen server");
				}
				else if (bigChange)
				{
					msg += message.COLOR_ORANGE;
					output = true;
					why.add("User change");
				}
				else if (s.preferred_links.isEmpty() == false)
				{
					boolean good = false;
					for (Iterator<String> it = s.preferred_links.iterator(); it.hasNext();)
					{
						server p_s = server.findServerAbsolute(it.next());
						if (p_s != null && isLink(s, p_s))
							good = true;
					}
					
					if (good == false)
					{
						output = true;
						why.add("Uplink not a preferred server");
					}
				}
				
				msg += "[Users: " + s.users + change + "] ";
				msg += s.getName();
				msg += message.COLOR_END;
				msg += " / " + links;
				
				links = "";
				for (Iterator<String> it = s.clines.iterator(); it.hasNext();)
				{
					String cline_name = it.next();
					server cline_server = server.findServerAbsolute(cline_name);
					
					if (cline_server == null)
						links += message.COLOR_RED;
					else if (cline_server.isServices())
						continue;
					else if (cline_server.getSplit() != null)
						links += message.COLOR_ORANGE;
					else if (cline_server.clines.contains(s.getName()) == false)
						links += message.COLOR_YELLOW;
					else if (cline_server.frozen)
						links += message.COLOR_BRIGHTBLUE;
					else if (all == false)
						continue;
					else
						links += message.COLOR_GREEN;
					
					output = true;
					
					if (cline_server != null)
					{
						if (isLink(s, cline_server))
							links += message.COLOR_UNDERLINE + cline_server.getName() + message.COLOR_UNDERLINE + " (" + cline_server.links.size() + ")";
						else
							links += cline_server.getName() + " (" + cline_server.links.size() + ")";
					}
					else
						links += cline_name;
					links += message.COLOR_END +  ", ";
				}
				if (links.isEmpty() == false)
					links = links.substring(0, links.length() - 2);
				else
					links = "N/A";
				
				msg += " / " + links;
				
				if (why.isEmpty() == false)
					msg += " / " + why.toString();
				
				if (output || all)
				{
					moo.reply(source, target, msg);
					all_output = true;
				}
			}
			
			int total_diff = server.cur_total_users - server.last_total_users;
			String change = String.valueOf(total_diff);
			if (change.startsWith("-") == false)
				change = "+" + change;
			
			if (match == null && (all_output || all))
				moo.reply(source, target, "Total Users: " + server.cur_total_users + change);
		}
	}
	
	@Override
	public void onHelp(String source)
	{
		moo.notice(source, "Syntax: " + this.getCommandName() + " [freeze|unfreeze|delete] server.name [all|preferred clines]");
		moo.notice(source, " ");
		moo.notice(source, this.getCommandName() + " is used to view C-Lines between servers and configure preferred clines,");
		moo.notice(source, "which are C-Lines that have preference over others. When servers split the auto reconnector will");
		moo.notice(source, "use this information to determine where to reconnect servers.");
		moo.notice(source, " ");
		moo.notice(source, "The output format of this command is as follows: users / preferred C-Lines / all C-Lines");
		moo.notice(source, "The number in parentheses next to C-Lines are the number of links active on that hub.");
		moo.notice(source, " ");
		moo.notice(source, "C-Lines are color coded to determine the state of the C-Line");
		moo.notice(source, " " + message.COLOR_RED + "red" + message.COLOR_END + " - Represents a C-Line to a nonexistant server");
		moo.notice(source, " " + message.COLOR_ORANGE + "orange" + message.COLOR_END + " - Represents a C-Line to a split server");
		moo.notice(source, " " + message.COLOR_YELLOW + "yellow" + message.COLOR_END + " - Represents a broken C-Line with only one direction (no return C-Line)");
		moo.notice(source, " " + message.COLOR_BRIGHTBLUE + "blue" + message.COLOR_END + " - Represents a C-Line to a frozen server");
		moo.notice(source, " " + message.COLOR_GREEN + "green" + message.COLOR_END + " - Represents a good C-Line");
		moo.notice(source, " " + message.COLOR_UNDERLINE + "Underlines" + message.COLOR_UNDERLINE + " are used to show what the current uplink for a server is.");
		moo.notice(source, "Frozen servers are made using the freeze command. Frozen servers will not be reconnected *nor* connected to.");
		moo.notice(source, " ");
		moo.notice(source, "Executing this command with no parameters will only show problem servers and and the problem C-Lines on those servers.");
		moo.notice(source, "When using ALL or searching for a specific name all C-Lines will be shown.");
	}
}

class commandCline extends commandServerBase
{
	public commandCline(mpackage pkg)
	{
		super(pkg, "!CLINE");
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length == 2)
			super.execute(source, target, params);
	}
}

public class commandServer
{
	public commandServer(mpackage pkg)
	{
		new commandServerBase(pkg, "!SERVER");
		new commandServerBase(pkg, ".SERVER");
		
		new commandCline(pkg);
	}
}
