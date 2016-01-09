package net.rizon.moo.plugin.watch;

import java.util.Date;
import java.util.Iterator;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.logging.logging;

class CommandWatch extends Command
{
	public CommandWatch(Plugin pkg)
	{
		super(pkg, "!WATCH", "View or modify the watch list");

		this.requiresChannel(Moo.conf.staff_channels);
		this.requiresChannel(Moo.conf.oper_channels);
		this.requiresChannel(Moo.conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax:");
		source.notice(this.getCommandName() + " LIST -- shows the watch list");
		source.notice(this.getCommandName() + " ADD <nick> [+expiry] [+C] <reason> -- adds an entry to the watch list");
		source.notice(this.getCommandName() + " DEL <nick/num> -- deletes an entry from the watch list");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length <= 1 || params[1].equalsIgnoreCase("list"))
		{
			if (watch.watches.isEmpty())
				source.reply("The watch list is empty");
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

					source.notice("" + ++count + ". " + e.nick + ", created on " + e.created + " by " + e.creator + ", expires on " + e.expires + ". Reason: " + e.reason);
				}
			}
		}
		else if (params[1].equalsIgnoreCase("add") && params.length > 3 && (Moo.conf.adminChannelsContains(source.getTargetName()) || Moo.conf.operChannelsContains(source.getTargetName())))
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
					source.reply("Expiry " + expires + " is not valid");
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
			we.creator = source.getUser().getNick();
			we.reason = reason;
			we.created = new Date();
			we.expires = new Date(System.currentTimeMillis() + (expires_len * 1000L));
			we.registered = state;

			source.reply("Watch added for " + we.nick + " to expire on " + we.expires);
			Moo.operwall(we.creator + " added a watch entry (" + (state == WatchEntry.registeredState.RS_MANUAL_CAPTURE ? "capture" : "akill") + ") for " + we.nick + " to expire on " + we.expires + ". Reason: " + reason);
			
			if (Plugin.findPlugin("logging") != null)
				logging.addEntry("WATCH", we.creator, we.nick, reason);
			
			// does insert or replace
			watch.insert(we);

			if (add)
				watch.watches.add(we);
		}
		else if (params[1].equals("del") && params.length > 2)
		{
			for (Iterator<WatchEntry> it = watch.watches.iterator(); it.hasNext();)
			{
				WatchEntry e = it.next();
				if (e.nick.equalsIgnoreCase(params[2]))
				{
					it.remove();
					source.reply("Watch for " + e.nick + " removed");
					return;
				}
			}

			try
			{
				WatchEntry e = watch.watches.get(Integer.parseInt(params[2]) - 1);
				watch.remove(e);
				if (watch.watches.remove(e))
				{
					source.reply("Watch for " + e.nick + " removed");
					return;
				}
			}
			catch (NumberFormatException | IndexOutOfBoundsException ex) { }

			source.reply("No watch for " + params[2] + " found");
		}
	}
}
