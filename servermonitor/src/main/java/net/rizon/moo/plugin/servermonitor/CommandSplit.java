package net.rizon.moo.plugin.servermonitor;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;
import net.rizon.moo.Split;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

class splitComparator implements Comparator<Split>
{
	@Override
	public int compare(Split arg0, Split arg1)
	{
		if (arg0.when.after(arg1.when))
			return 1;
		else if (arg0.when.before(arg1.when))
			return -1;
		return 0;
	}
}

class CommandSplit extends Command
{
	public CommandSplit(Plugin pkg)
	{
		super(pkg, "!SPLIT", "Views split servers");

		this.requiresChannel(Moo.conf.staff_channels);
		this.requiresChannel(Moo.conf.oper_channels);
		this.requiresChannel(Moo.conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: " + this.getCommandName() + " [recent|del|freeze|unfreeze|server.name]");
		source.notice(" ");
		source.notice("With no arguments shows currently split servers. With recent or server.name only");
		source.notice("recent splits or splits from that server are shown. Freeze and unfreeze can be used");
		source.notice("to globally freeze and unfreeze all servers, including past and future ones. Del");
		source.notice("(or stop) will disable a reconnect for a server.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length <= 1)
		{
			Date now = new Date();
			int count = 0, split = 0;

			for (Server s : Server.getServers())
			{
				Split sp = s.getSplit();
				++count;

				if (sp != null || s.links.isEmpty())
				{
					++split;
					String s_name;
					if (s.frozen || !servermonitor.conf.reconnect)
						s_name = Message.COLOR_BRIGHTBLUE + s.getName() + Message.COLOR_END;
					else
						s_name = s.getName();
					String buffer;
					if (sp != null)
						buffer = "[SPLIT] " + s_name + " <-> " + sp.from + ", " + Moo.difference(now, sp.when) + " ago.";
					else
						buffer = "[SPLIT] " + s_name + ".";
					Reconnector r = Reconnector.findValidReconnectorFor(s);
					if (r != null)
					{
						Server to = r.findPreferred();
						if (to == s)
							buffer += " Delaying due to uplink being split.";
						else
							buffer += " Will reconnect in " + Moo.difference(now, r.reconnectTime()) + " to " + r.findPreferred().getName() + ".";
					}
					source.reply(buffer);
				}
			}

			source.reply("[SPLIT] [" + split + "/" + count + "]");
		}
		else if (params[1].equalsIgnoreCase("recent"))
		{
			boolean all = params.length > 2 && params[2].equalsIgnoreCase("all");
			TreeSet<Split> ts = new TreeSet<Split>(new splitComparator());
			Date now = new Date();

			for (Server s : Server.getServers())
			{
				Split[] splits = s.getSplits();

				for (int i = splits.length; i > 0; --i)
				{
					Split split = splits[i - 1];
					if (all || !split.recursive)
						ts.add(split);
				}
			}

			int count = 10;
			try
			{
				count = Integer.parseInt(params[2]);
			}
			catch (Exception ex) { }

			while (ts.size() > count)
				ts.remove(ts.first());

			if (ts.isEmpty())
				source.reply("There are no recent splits");
			else
			{
				source.reply("Recent splits:");

				for (Iterator<Split> it = ts.descendingIterator(); it.hasNext();)
				{
					Split sp = it.next();

					String buf = "[SPLIT] " + sp.me + " <-> " + sp.from + ", " + Moo.difference(now, sp.when) + " ago.";
					if (sp.end != null && sp.to != null)
					{
						buf += " Reconnected to " + sp.to + " " + Moo.difference(sp.end, sp.when) + " later";
						if (sp.reconnectedBy != null)
							buf += " by " + sp.reconnectedBy;
						buf += ".";
					}
					else
					{
						Server s = Server.findServerAbsolute(sp.me);
						if (s != null)
						{
							Reconnector r = Reconnector.findValidReconnectorFor(s);
							if (r != null)
								buf += " Will reconnect in " + Moo.difference(now, r.reconnectTime()) + " to " + r.findPreferred().getName() + ".";
						}
					}

					source.reply(buf);
				}
			}
		}
		else if (params.length > 2 && params[1].equalsIgnoreCase("del"))
		{
			Server s = Server.findServer(params[2]);
			if (s == null)
				source.reply("[SPLIT] Server " + params[2] + " not found");
			else if (s.getSplit() == null)
				source.reply("[SPLIT] Server " + s.getName() + " is not marked as split");
			else
			{
				source.reply("Deleted server " + s.getName());
				s.destroy();
			}
		}
		else if (params.length > 2 && params[1].equalsIgnoreCase("stop"))
		{
			Server s = Server.findServer(params[2]);
			if (s == null)
				source.reply("[SPLIT] Server " + params[2] + " not found");
			else if (s.getSplit() == null)
				source.reply("[SPLIT] Server " + s.getName() + " is not marked as split");
			else if (Reconnector.removeReconnectsFor(s))
				source.reply("[SPLIT] Removed reconnect for server " + s.getName());
			else
				source.reply("[SPLIT] There are no pending reconnects for " + s.getName());
		}
		else if (params[1].equalsIgnoreCase("freeze"))
		{
			servermonitor.conf.reconnect = false;
			for (Server s : Server.getServers())
				Reconnector.removeReconnectsFor(s);
			source.reply("[SPLIT] Disabled all reconnects and all future reconnects");
		}
		else if (params[1].equalsIgnoreCase("unfreeze"))
		{
			servermonitor.conf.reconnect = true;
			source.reply("[SPLIT] Reenabled reconnects");
		}
		else
		{
			Server s = Server.findServer(params[1]);
			Date now = new Date();

			if (s == null)
			{
				source.reply("No such server " + params[1]);
				return;
			}

			boolean all = params.length > 2 && params[2].equalsIgnoreCase("all");
			List<Split> splits = new ArrayList<Split>();
			for (Split split : s.getSplits())
				if (all || !split.recursive)
					splits.add(split);

			if (splits.isEmpty())
			{
				source.reply(s.getName() + " has never split");
				return;
			}

			source.reply("Recent splits for " + s.getName() + ":");

			int count = 3;
			try
			{
				count = Integer.parseInt(params[2]);
			}
			catch (Exception ex) { }

			for (int i = splits.size(); i > 0 && count > 0; --i, --count)
			{
				Split sp = splits.get(i - 1);

				String buf = "[SPLIT] " + s.getName() + " <-> " + sp.from + ", " + Moo.difference(now, sp.when) + " ago.";
				if (sp.end != null && sp.to != null)
				{
					buf += " Reconnected to " + sp.to + " " + Moo.difference(sp.end, sp.when) + " later";
					if (sp.reconnectedBy != null)
						buf += " by " + sp.reconnectedBy;
					buf += ".";
				}
				else
				{
					Reconnector r = Reconnector.findValidReconnectorFor(s);
					if (r != null)
						buf += " Will reconnect in " + Moo.difference(now, r.reconnectTime()) + " to " + r.findPreferred().getName() + ".";
				}

				source.reply(buf);
			}
		}
	}
}
