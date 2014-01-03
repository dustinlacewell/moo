package net.rizon.moo.servermonitor;

import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;

import net.rizon.moo.Command;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;
import net.rizon.moo.Split;

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
		
		this.requiresChannel(Moo.conf.getList("staff_channels"));
		this.requiresChannel(Moo.conf.getList("oper_channels"));
		this.requiresChannel(Moo.conf.getList("admin_channels"));
	}
	
	@Override
	public void onHelp(String source)
	{
		Moo.notice(source, "Syntax: " + this.getCommandName() + " [recent|del|freeze|unfreeze|server.name]");
		Moo.notice(source, " ");
		Moo.notice(source, "With no arguments shows currently split servers. With recent or server.name only");
		Moo.notice(source, "recent splits or splits from that server are shown. Freeze and unfreeze can be used");
		Moo.notice(source, "to globally freeze and unfreeze all servers, including past and future ones. Del");
		Moo.notice(source, "(or stop) will disable a reconnect for a server.");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length <= 1)
		{
			Date now = new Date();
			int count = 0, split = 0;

			for (Server s : Server.getServers())
			{
				Split sp = s.getSplit();
				++count;
				
				if (sp != null)
				{
					++split;
					String s_name;
					if (s.frozen || Moo.conf.getBool("disable_split_reconnect"))
						s_name = Message.COLOR_BRIGHTBLUE + s.getName() + Message.COLOR_END;
					else
						s_name = s.getName();
					String buffer = "[SPLIT] " + s_name + " <-> " + sp.from + ", " + Moo.difference(now, sp.when) + " ago.";
					Reconnector r = Reconnector.findValidReconnectorFor(s);
					if (r != null)
						buffer += " Will reconnect in " + Moo.difference(now, r.reconnectTime()) + " to " + r.findPreferred().getName() + ".";
					Moo.reply(source, target, buffer);
				}
			}
			
			Moo.reply(source, target, "[SPLIT] [" + split + "/" + count + "]");
		}
		else if (params[1].equalsIgnoreCase("recent"))
		{
			TreeSet<Split> ts = new TreeSet<Split>(new splitComparator());
			Date now = new Date();
			
			for (Server s : Server.getServers())
			{
				Split[] splits = s.getSplits();
				
				for (int i = splits.length; i > 0; --i)
					ts.add(splits[i - 1]);
			}
			
			int count = 10;
			try
			{
				count = Integer.parseInt(params[2]);
			}
			catch (Exception ex) { }
			
			while (ts.size() > count)
				ts.remove(ts.first());
			
			if (ts.size() == 0)
				Moo.reply(source, target, "There are no recent splits");
			else
			{
				Moo.reply(source, target, "Recent splits:");

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
					
					Moo.reply(source, target, buf);
				}
			}
		}
		else if (params.length > 2 && params[1].equalsIgnoreCase("del"))
		{
			Server s = Server.findServer(params[2]);
			if (s == null)
				Moo.reply(source, target, "[SPLIT] Server " + params[2] + " not found");
			else if (s.getSplit() == null)
				Moo.reply(source, target, "[SPLIT] Server " + s.getName() + " is not marked as split");
			else
			{
				Moo.reply(source, target, "Deleted server " + s.getName());
				s.destroy();
			}
		}
		else if (params.length > 2 && params[1].equalsIgnoreCase("stop"))
		{
			Server s = Server.findServer(params[2]);
			if (s == null)
				Moo.reply(source, target, "[SPLIT] Server " + params[2] + " not found");
			else if (s.getSplit() == null)
				Moo.reply(source, target, "[SPLIT] Server " + s.getName() + " is not marked as split");
			else if (Reconnector.removeReconnectsFor(s))
				Moo.reply(source, target, "[SPLIT] Removed reconnect for server " + s.getName());
			else
				Moo.reply(source, target, "[SPLIT] There are no pending reconnects for " + s.getName());
		}
		else if (params[1].equalsIgnoreCase("freeze"))
		{
			Moo.conf.setBoolean("disable_split_reconnect", true);
			for (Server s : Server.getServers())
				Reconnector.removeReconnectsFor(s);
			Moo.reply(source, target, "[SPLIT] Disabled all reconnects and all future reconnects");
		}
		else if (params[1].equalsIgnoreCase("unfreeze"))
		{
			Moo.conf.setBoolean("disable_split_reconnect", false);
			Moo.reply(source, target, "[SPLIT] Reenabled reconnects");
		}
		else
		{
			Server s = Server.findServer(params[1]);
			Date now = new Date();
			
			if (s == null)
				Moo.reply(source, target, "No such server " + params[1]);
			else
			{
				Split[] splits = s.getSplits();
				
				if (splits.length == 0)
					Moo.reply(source, target, s.getName() + " has never split");
				else
				{
					Moo.reply(source, target, "Recent splits for " + s.getName() + ":");
					
					int count = 3;
					try
					{
						count = Integer.parseInt(params[2]);
					}
					catch (Exception ex) { }
					
					for (int i = splits.length; i > 0 && count > 0; --i, --count)
					{
						Split sp = splits[i - 1];
						
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
						
						Moo.reply(source, target, buf);
					}
				}
			}
		}
	}
}
