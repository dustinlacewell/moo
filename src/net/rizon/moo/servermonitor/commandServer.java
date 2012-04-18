package net.rizon.moo.servermonitor;

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
	
	private static final server getUplink(server s)
	{
		if (s.isHub() || s.isServices())
			return null;
		
		for (Iterator<String> it = s.links.iterator(); it.hasNext();)
		{
			server s2 = server.findServerAbsolute(it.next());
			if (s2 == null)
				continue;
			else if (s2.isHub())
				return s2;
		}
		
		return null;
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
				all = params.length > 2 && params[2].equalsIgnoreCase("ALL");
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
				
				int diff = s.users - s.last_users;
				String change = String.valueOf(diff);
				if (diff == 0)
					change = "";
				else if (change.startsWith("-") == false)
					change = "+" + change;
				
				String links = "";
				for (Iterator<String> it = s.preferred_links.iterator(); it.hasNext();)
				{
					String link_name = it.next();
					server link_server = server.findServerAbsolute(link_name);
					
					if (link_server == null)
					{
						links += message.COLOR_RED;
						output = true;
					}
					else if (link_server.getSplit() != null)
					{
						links += message.COLOR_ORANGE;
						output = true;
					}
					else if (s.clines.contains(link_server.getName()) == false)
					{
						links += message.COLOR_YELLOW;
						output = true;
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
						if (s == getUplink(link_server))
							links += message.COLOR_UNDERLINE + link_server.getName() + message.COLOR_END + " (" + link_server.links.size() + ")";
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
				}
				else if (s.frozen || moo.conf.getDisableSplitReconnect())
				{
					msg += message.COLOR_BRIGHTBLUE;
					output = true;
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
						if (cline_server == getUplink(s))
							links += message.COLOR_UNDERLINE + cline_server.getName() + message.COLOR_END + " (" + cline_server.links.size() + ")";
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
}

public class commandServer
{
	public commandServer(mpackage pkg)
	{
		new commandServerBase(pkg, "!SERVER");
		new commandServerBase(pkg, ".SERVER");
	}
}
