package net.rizon.moo.servermonitor;

import java.util.HashSet;
import java.util.Iterator;

import net.rizon.moo.Command;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;

class commandServerBase extends Command
{
	public commandServerBase(Plugin pkg, final String command)
	{
		super(pkg, command, "Views servers");
	}
	
	private static boolean isLink(Server s, Server targ)
	{
		for (Iterator<String> it = s.links.iterator(); it.hasNext();)
		{
			Server s2 = Server.findServerAbsolute(it.next());
			
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
			Server s = Server.findServer(params[2]);
			if (s == null)
				Moo.reply(source, target, "No such server " + params[2]);
			else if (s.frozen == true)
				Moo.reply(source, target, s.getName() + " is already frozen");
			else
			{
				s.frozen = true;
				Moo.reply(source, target, "Froze server " + s.getName());
			}
		}
		else if (params.length >= 3 && params[1].equalsIgnoreCase("UNFREEZE"))
		{
			Server s = Server.findServer(params[2]);
			if (s == null)
				Moo.reply(source, target, "No such server " + params[2]);
			else if (s.frozen == false)
				Moo.reply(source, target, s.getName() + " is already unfrozen");
			else
			{
				s.frozen = false;
				Moo.reply(source, target, "Unfroze server " + s.getName());
			}
		}
		else if (params.length >= 3 && (params[1].equalsIgnoreCase("DELETE") || params[1].equalsIgnoreCase("DEL")))
		{
			Server s = Server.findServerAbsolute(params[2]);
			if (s == null)
				Moo.reply(source, target, "No such server " + params[2] + ", full name required");
			else
			{
				Moo.reply(source, target, "Deleted server " + s.getName());
				s.destroy();
			}
		}
		else if (params.length >= 3 && params[1].equalsIgnoreCase("DESC"))
		{
			Server s = Server.findServer(params[2]);
			if (s == null)
				Moo.reply(source, target, "No such server " + params[2]);
			else if (params.length == 3)
			{
				s.setDesc("");
				Moo.reply(source, target, "Description for " + s.getName() + " unset");
			}
			else
			{
				String desc = params[3];
				for (int i = 4; i < params.length; ++i)
					desc += " " + params[i];
				s.setDesc(desc);
				Moo.reply(source, target, "Description for " + s.getName() + " updated");
			}
		}
		else if (params.length >= 3)
		{
			Server s = Server.findServer(params[1]);
			if (s == null)
				Moo.reply(source, target, "Server " + params[1] + " not found");
			else if (params[2].equals("."))
			{
				s.preferred_links.clear();
				Moo.reply(source, target, "Prefered links for " + s.getName() + " unset");
			}
			else
			{
				boolean modified = false;
				
				for (int i = 2; i < params.length; ++i)
				{
					Server arg = Server.findServer(params[i].substring(1));
					if (arg == null)
					{
						Moo.reply(source, target, "No such server: " + params[i]);
						continue;
					}
					else if (arg == s)
					{
						Moo.reply(source, target, "Servers can not link to themselves");
						continue;
					}
					else if (modified == true && s.preferred_links.contains(arg.getName()))
					{
						Moo.reply(source, target, s.getName() + " already has " + arg.getName());
						continue;
					}
					else if (arg.isHub() == false)
					{
						Moo.reply(source, target, "You may only link servers to hubs");
						continue;
					}
					
					if (modified == false)
					{
						s.preferred_links.clear();
						modified = true;
					}
					s.preferred_links.add(arg.getName());
				}
				
				Moo.reply(source, target, "Prefered links for " + s.getName() + " set to " + s.preferred_links.toString());
			}
		}
		else
		{
			boolean all = params.length > 1 && params[1].equalsIgnoreCase("ALL");
			String match = params.length > 1 && all == false ? params[1] : null;
			if (match != null)
				all = true;
			boolean all_output = false;
			for (Server s : Server.getServers())
			{
				boolean output = false;
				
				if (match != null)
				{
					if (Moo.matches(s.getName(), "*" + match + "*"))
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
					Server link_server = Server.findServerAbsolute(link_name);
					
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
				else if (s.frozen || Moo.conf.getBool("disable_split_reconnect"))
				{
					msg += Message.COLOR_BRIGHTBLUE;
					output = true;
				}
				else if (bigChange)
				{
					msg += Message.COLOR_ORANGE;
					output = true;
				}
				else if (s.preferred_links.isEmpty() == false)
				{
					boolean good = false;
					for (Iterator<String> it = s.preferred_links.iterator(); it.hasNext();)
					{
						Server p_s = Server.findServerAbsolute(it.next());
						if (p_s != null && isLink(s, p_s))
							good = true;
					}
					
					if (good == false)
					{
						output = true;
						showAllCLines = true;
						why.add("Uplink not a preferred server");
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
						Server link = Server.findServerAbsolute(linkname);
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
					Server cline_server = Server.findServerAbsolute(cline_name);
					
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
					Moo.reply(source, target, msg);
					all_output = true;
				}
			}
			
			int total_diff = Server.cur_total_users - Server.last_total_users;
			String change = String.valueOf(total_diff);
			if (change.startsWith("-") == false)
				change = "+" + change;
			
			if (match == null && (all_output || all))
				Moo.reply(source, target, "Total Users: " + Server.cur_total_users + change);
		}
	}
	
	@Override
	public void onHelp(String source)
	{
		Moo.notice(source, "Syntax: " + this.getCommandName() + " [freeze|unfreeze|delete] server.name [.|all|preferred clines]");
		Moo.notice(source, " ");
		Moo.notice(source, this.getCommandName() + " is used to view C-Lines between servers and configure preferred C-Lines,");
		Moo.notice(source, "which are C-Lines that have preference over others. When servers split the auto reconnector will");
		Moo.notice(source, "use this information to determine where to reconnect servers.");
		Moo.notice(source, " ");
		Moo.notice(source, "The output format of this command is as follows: users / preferred C-Lines / all C-Lines");
		Moo.notice(source, "The number in parentheses next to C-Lines are the number of links active on that hub.");
		Moo.notice(source, " ");
		Moo.notice(source, "C-Lines are color coded to determine the state of the C-Line");
		Moo.notice(source, " " + Message.COLOR_RED + "red" + Message.COLOR_END + " - Represents a C-Line to a nonexistent server");
		Moo.notice(source, " " + Message.COLOR_ORANGE + "orange" + Message.COLOR_END + " - Represents a C-Line to a split server");
		Moo.notice(source, " " + Message.COLOR_YELLOW + "yellow" + Message.COLOR_END + " - Represents a broken C-Line with only one direction (no return C-Line)");
		Moo.notice(source, " " + Message.COLOR_BRIGHTBLUE + "blue" + Message.COLOR_END + " - Represents a C-Line to a frozen server");
		Moo.notice(source, " " + Message.COLOR_GREEN + "green" + Message.COLOR_END + " - Represents a good C-Line");
		Moo.notice(source, " " + Message.COLOR_UNDERLINE + "Underlines" + Message.COLOR_UNDERLINE + " are used to show what the current uplink for a server is.");
		Moo.notice(source, "Frozen servers are made using the freeze command. Frozen servers will not be connected to, but may be reconnected to the network.");
		Moo.notice(source, " ");
		Moo.notice(source, "Executing this command with no parameters will only show problem servers and and the problem C-Lines on those servers.");
		Moo.notice(source, "When using ALL or searching for a specific name all C-Lines will be shown.");
		Moo.notice(source, "If a single dot is provided after server.name, all preferred C-Lines will be unset.");
	}
}

class commandCline extends commandServerBase
{
	public commandCline(Plugin pkg)
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

class CommandServer
{
	private Command e, d, c;
	
	public CommandServer(Plugin pkg)
	{
		e = new commandServerBase(pkg, "!SERVER");
		d = new commandServerBase(pkg, ".SERVER");
		
		c = new commandCline(pkg);
	}
	
	public void remove()
	{
		e.remove();
		d.remove();
		
		c.remove();
	}
}
