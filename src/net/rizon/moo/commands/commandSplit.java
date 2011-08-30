package net.rizon.moo.commands;

import java.util.Date;
import java.util.Iterator;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.server;

public class commandSplit extends command
{
	public commandSplit()
	{
		super("SPLIT");
	}
	
	private static String ago(Date now, Date then)
	{
		long lnow = now.getTime() / 1000, lthen = then.getTime() / 1000;
		
		long ldiff = lnow - lthen;
		int days = 0, hours = 0, minutes = 0;
		
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
		buffer += "ago";
		
		return buffer;
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length == 1)
		{
			Date now = new Date();
			int count = 0, split = 0;

			for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
			{
				server s = it.next();
				++count;
				
				if (s.isSplit())
				{
					++split;
					moo.sock.privmsg(target, "[SPLIT] " + s.getName() + " <-> " + s.split_from + ", " + ago(now, s.split_when));
				}
			}
			
			moo.sock.privmsg(target, "[SPLIT] [" + split + "/" + count + "]");
		}
		else if (params.length > 2 && params[1].equalsIgnoreCase("del"))
		{
			server s = server.findServer(params[2]);
			if (s == null)
				moo.sock.privmsg(target, "[SPLIT] Server " + params[2] + " not found");
			else if (s.isSplit() == false)
				moo.sock.privmsg(target, "[SPLIT] Server " + s.getName() + " is not marked as split");
			else
			{
				moo.sock.privmsg(target, "[SPLIT] Removed server " + s.getName());
				s.splitDel();
				s.destroy();
			}
		}
		else
			moo.sock.privmsg(target, "Syntax: !split [del server]");
	}
}
