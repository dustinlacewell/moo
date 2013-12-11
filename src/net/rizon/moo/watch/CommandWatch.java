package net.rizon.moo.watch;

import java.util.Date;
import java.util.Iterator;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.MPackage;

class CommandWatch extends Command
{
	public CommandWatch(MPackage pkg)
	{
		super(pkg, "!WATCH", "View or modify the watch list");
	}
	
	@Override
	public void onHelp(String source)
	{
		Moo.notice(source, "Syntax:");
		Moo.notice(source, this.getCommandName() + " LIST -- shows the watch list");
		Moo.notice(source, this.getCommandName() + " ADD <nick> [+expiry] [+C] <reason> -- adds an entry to the watch list");
		Moo.notice(source, this.getCommandName() + " DEL <nick> -- deletes an entry from the watch list");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length <= 1 || params[1].equalsIgnoreCase("list"))
		{
			if (watch.watches.isEmpty())
				Moo.notice(source, "The watch list is empty");
			else
			{
				Date now = new Date();
				int count = 0;

				for (Iterator<WatchEntry> it = watch.watches.iterator(); it.hasNext();)
				{
					WatchEntry e = it.next();
					
					if (e.expires.before(now))
					{
						it.remove();
						continue;
					}
					
					Moo.notice(source, "" + ++count + ". " + e.nick + ", created on " + e.created + " by " + e.creator + ", expires on " + e.expires + ". Reason: " + e.reason);
				}
			}
		}
		else if (params[1].equalsIgnoreCase("add") && params.length > 3 && (Moo.conf.isAdminChannel(target) || Moo.conf.isOperChannel(target)))
		{
			WatchEntry we = null;
			for (Iterator<WatchEntry> it = watch.watches.iterator(); it.hasNext();)
			{
				WatchEntry e = it.next();
				if (e.nick.equalsIgnoreCase(params[2]))
				{
					we = e;
					break;
				}
			}
			
			boolean add = false;
			if (we == null)
			{
				we = new WatchEntry();
				add = true;
			}
			
			WatchEntry.registeredState state = WatchEntry.registeredState.RS_MANUAL_AKILL;
			int reason_start = 4;
			String expires = params[3];
			long expires_len = 3 * 86400;
			if (expires.startsWith("+"))
			{
				int multiplier = 1;
				if (expires.endsWith("d"))
					multiplier = 86400;
				else if (expires.endsWith("m"))
					multiplier = 60;
				
				expires = expires.substring(1);
				while (!expires.isEmpty() && Character.isLetter(expires.charAt(expires.length() - 1)))
					expires = expires.substring(0, expires.length() - 1);
				
				int len = 0;
				try
				{
					len = Integer.parseInt(expires);
					expires_len = len * multiplier;
				}
				catch (NumberFormatException ex)
				{
					Moo.reply(source, target, "Expiry " + expires + " is not valid");
					return;
				}
				
				if (reason_start < params.length && params[reason_start].equalsIgnoreCase("+C"))
				{
					++reason_start;
					state = WatchEntry.registeredState.RS_MANUAL_CAPTURE;
				}
			}
			else
				reason_start = 3;
			
			String reason = "";
			for (; reason_start < params.length; ++reason_start)
				reason += params[reason_start] + " ";
			if (reason.isEmpty())
				reason = "No reason";
			
			we.nick = params[2];
			we.creator = source;
			we.reason = reason;
			we.created = new Date();
			we.expires = new Date(System.currentTimeMillis() + (expires_len * 1000L));
			we.registered = state;
			
			Moo.reply(source, target, "Watch added for " + we.nick + " to expire on " + we.expires);
			
			if (add)
				watch.watches.push(we);
		}
		else if (params[1].equals("del") && params.length > 2)
		{
			for (Iterator<WatchEntry> it = watch.watches.iterator(); it.hasNext();)
			{
				WatchEntry e = it.next();
				if (e.nick.equalsIgnoreCase(params[2]))
				{
					it.remove();
					Moo.reply(source, target, "Watch for " + e.nick + " removed");
					return;
				}
			}
			
			Moo.reply(source, target, "No watch for " + params[2] + " found");
		}
	}
}
