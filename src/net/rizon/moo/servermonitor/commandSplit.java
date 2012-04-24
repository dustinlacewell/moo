package net.rizon.moo.servermonitor;

import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;

import net.rizon.moo.command;
import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;
import net.rizon.moo.split;

class splitComparator implements Comparator<split>
{
	public int compare(split arg0, split arg1)
	{
		if (arg0.when.after(arg1.when))
			return 1;
		else if (arg0.when.before(arg1.when))
			return -1;
		return 0;
	}
}

public class commandSplit extends command
{
	public commandSplit(mpackage pkg)
	{
		super(pkg, "!SPLIT", "Views split servers");
	}
	
	private static String difference(Date now, Date then)
	{
		long lnow = now.getTime() / 1000L, lthen = then.getTime() / 1000L;
		
		long ldiff = now.compareTo(then) > 0 ? lnow - lthen : lthen - lnow;
		int days = 0, hours = 0, minutes = 0;
		
		if (ldiff == 0)
			return "0 seconds";
		
		while (ldiff > 86400)
		{
			++days;
			ldiff -= 86400;
		}
		while (ldiff > 3600)
		{
			++hours;
			ldiff -= 3600;
		}
		while (ldiff > 60)
		{
			++minutes;
			ldiff -= 60;
		}
		
		String buffer = "";
		if (days > 0)
			buffer += days + " day" + (days == 1 ? "" : "s") + " ";
		if (hours > 0)
			buffer += hours + " hour" + (hours == 1 ? "" : "s") + " ";
		if (minutes > 0)
			buffer += minutes + " minute" + (minutes == 1 ? "" : "s") + " ";
		if (ldiff > 0)
			buffer += ldiff + " second" + (ldiff == 1 ? "" : "s") + " ";
		buffer = buffer.trim();
		
		return buffer;
	}
	
	@Override
	public void onHelp(String source)
	{
		moo.notice(source, "Syntax: " + this.getCommandName() + " [recent|del|freeze|unfreeze|server.name]");
		moo.notice(source, " ");
		moo.notice(source, "With no arguments shows currently split servers. With recent or server.name only");
		moo.notice(source, "recent splits or splits from that server are shown. Freeze and unfreeze can be used");
		moo.notice(source, "to globally freeze and unfreeze all servers, including past and future ones. Del");
		moo.notice(source, "(or stop) will disable a reconnect for a server.");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length <= 1)
		{
			Date now = new Date();
			int count = 0, split = 0;

			for (server s : server.getServers())
			{
				split sp = s.getSplit();
				++count;
				
				if (sp != null)
				{
					++split;
					String s_name;
					if (s.frozen || moo.conf.getDisableSplitReconnect())
						s_name = message.COLOR_BRIGHTBLUE + s.getName() + message.COLOR_END;
					else
						s_name = s.getName();
					String buffer = "[SPLIT] " + s_name + " <-> " + sp.from + ", " + difference(now, sp.when) + " ago.";
					reconnector r = reconnector.findValidReconnectorFor(s);
					if (r != null)
						buffer += " Will reconnect in " + difference(now, r.reconnectTime()) + ".";
					moo.reply(source, target, buffer);
				}
			}
			
			moo.reply(source, target, "[SPLIT] [" + split + "/" + count + "]");
		}
		else if (params[1].equalsIgnoreCase("recent"))
		{
			TreeSet<split> ts = new TreeSet<split>(new splitComparator());
			Date now = new Date();
			
			for (server s : server.getServers())
			{
				split[] splits = s.getSplits();
				
				for (int i = splits.length; i > 0; --i)
					ts.add(splits[i - 1]);
			}
			
			while (ts.size() > 10)
				ts.remove(ts.first());
			
			if (ts.size() == 0)
				moo.reply(source, target, "There are no recent splits");
			else
			{
				moo.reply(source, target, "Recent splits:");

				for (Iterator<split> it = ts.descendingIterator(); it.hasNext();)
				{
					split sp = it.next();
					
					String buf = "[SPLIT] " + sp.me + " <-> " + sp.from + ", " + difference(now, sp.when) + " ago.";
					if (sp.end != null && sp.to != null)
						buf += " Reconnected to " + sp.to + " " + difference(sp.end, sp.when) + " later.";
					else
					{
						server s = server.findServerAbsolute(sp.me);
						if (s != null)
						{
							reconnector r = reconnector.findValidReconnectorFor(s);
							if (r != null)
								buf += " Will reconnect in " + difference(now, r.reconnectTime()) + ".";
						}
					}
					
					moo.reply(source, target, buf);
				}
			}
		}
		else if (params.length > 2 && (params[1].equalsIgnoreCase("stop") || params[1].equalsIgnoreCase("del")))
		{
			server s = server.findServer(params[2]);
			if (s == null)
				moo.reply(source, target, "[SPLIT] Server " + params[2] + " not found");
			else if (s.getSplit() == null)
				moo.reply(source, target, "[SPLIT] Server " + s.getName() + " is not marked as split");
			else
			{
				moo.reply(source, target, "[SPLIT] Removed reconnect for server " + s.getName());
				reconnector.removeReconnectsFor(s);
			}
		}
		else if (params[1].equalsIgnoreCase("freeze"))
		{
			moo.conf.setDisableSplitReconnect(true);
			for (server s : server.getServers())
				reconnector.removeReconnectsFor(s);
			moo.reply(source, target, "[SPLIT] Disabled all reconnects and all future reconnects");
		}
		else if (params[1].equalsIgnoreCase("unfreeze"))
		{
			moo.conf.setDisableSplitReconnect(false);
			moo.reply(source, target, "[SPLIT] Reenabled reconnects");
		}
		else
		{
			server s = server.findServer(params[1]);
			Date now = new Date();
			
			if (s == null)
				moo.reply(source, target, "No such server " + params[1]);
			else
			{
				split[] splits = s.getSplits();
				
				if (splits.length == 0)
					moo.reply(source, target, s.getName() + " has never split");
				else
				{
					moo.reply(source, target, "Recent splits for " + s.getName() + ":");
					for (int i = splits.length; i > 0; --i)
					{
						split sp = splits[i - 1];
						
						String buf = "[SPLIT] " + s.getName() + " <-> " + sp.from + ", " + difference(now, sp.when) + " ago.";
						if (sp.end != null && sp.to != null)
							buf += " Reconnected to " + sp.to + " " + difference(sp.end, sp.when) + " later.";
						else
						{
							reconnector r = reconnector.findValidReconnectorFor(s);
							if (r != null)
								buf += " Will reconnect in " + difference(now, r.reconnectTime()) + ".";
						}
						
						moo.reply(source, target, buf);
					}
				}
			}
		}
	}
}
