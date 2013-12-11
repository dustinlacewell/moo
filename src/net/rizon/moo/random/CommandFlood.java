package net.rizon.moo.random;

import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

class CommandFlood extends Command
{
	public CommandFlood(Plugin pkg)
	{
		super(pkg, "!FLOOD", "Manage flood lists");
		this.requiresChannel(Moo.conf.getFloodChannels());
	}

	@Override
	public void onHelp(String source)
	{
		Moo.notice(source, "!FLOOD keeps track of flood lists, which are created when there is a connection");
		Moo.notice(source, "flood from clients.");
		Moo.notice(source, "Syntax:");
		Moo.notice(source, "!FLOOD <flood list number> LIST -- Displays all entries in a flood list");
		Moo.notice(source, "!FLOOD <flood list number> DEL [host/range] -- Deletes selected entries from a flood list or the entire list");
		Moo.notice(source, "!FLOOD <flood list number> AKILL [duration] --  Akills the entire list. If no duration is given, 2d will be assumed");
		Moo.notice(source, "!FLOOD DESTROY <number> --  Akills everything on at least <number> lists");
		Moo.notice(source, "!FLOOD <flood list number> APPLY <regex> -- Delete all entries that don't match the given regex matched against nick");
		Moo.notice(source, "!FLOOD <flood list number> APPLY <number> -- Delete all entries aren't on <number> floodlists");
		Moo.notice(source, "!FLOOD <flood list number> APPLYW <wildcard match> -- Delete all entries don't match the given wildcard expression (Used wildcards: ? and *)");
		Moo.notice(source, "!FLOOD <flood list number> APPLYF <nick!user@host/gecos regex> -- Delete all entries that don't match the given regex matched against regex");
		Moo.notice(source, "!FLOOD LIST -- Lists all available flood lists");
		Moo.notice(source, "!FLOOD CLEAR -- Deletes all available flood lists");
	}
	
	private int purge()
	{
		int removed = 0;
		
		for (Iterator<FloodList> it = FloodList.getLists().iterator(); it.hasNext();)
		{
			FloodList fl = it.next();
			
			for (Iterator<NickData> it2 = fl.getMatches().iterator(); it2.hasNext();)
			{
				NickData nd = it2.next();
				
				if (nd.akilled)
				{
					it2.remove();
					++removed;
				}
			}
			
			if (fl.getMatches().isEmpty())
				it.remove();
		}
		
		return removed;
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length == 1)
		{
			this.onHelp(source);
			return;
		}
		else if (params[1].equalsIgnoreCase("LIST"))
		{
			if (FloodList.getActiveLists().isEmpty())
				Moo.privmsg(target, "There are no flood lists.");
			else
			{
				int i = 1;
				for (Iterator<FloodList> it = FloodList.getActiveLists().iterator(); it.hasNext();)
				{
					FloodList p = it.next();
					
					Moo.reply(source, target, i++ + ": Contains " + p.getMatches().size() + " entries, last modified: " + new Date(p.getTimes().getLast() * 1000L) + ", pattern: " + p.toString());
				}
			}
			return;
		}
		else if (params[1].equalsIgnoreCase("STATE"))
		{
			Moo.reply(source, target, "Nicks in history: " + random.getNicks().size());
			if (random.getNicks().isEmpty() == false)
				Moo.reply(source, target, "  Oldest: " + new Date(random.getNicks().getFirst().time * 1000L) + ", Newest: " + new Date(random.getNicks().getLast().time * 1000L));
			Moo.reply(source, target, "Patterns: " + FloodList.getLists().size());
			for (Iterator<FloodList> it = FloodList.getLists().iterator(); it.hasNext();)
			{
				FloodList p = it.next();
				Moo.reply(source, target, "  " + p.toString() + ": contains " + p.getMatches().size() + " matches, first at " + new Date(p.getTimes().getFirst() * 1000L) + ", last at " + new Date(p.getTimes().getLast() * 1000L));
			}
			Moo.reply(source, target, "Threshold: " + random.matchesForFlood + " in " + random.timeforMatches + " seconds");
			return;
		}
		else if (params[1].equalsIgnoreCase("DESTROY"))
		{
			int min = 0;
			
			try
			{
				min = Integer.parseInt(params[2]);
			}
			catch (Exception ex)
			{
			}
			
			int lists = FloodList.getActiveLists().size(), count = 0, dupes = 0;
			
			for (Iterator<FloodList> it = FloodList.getActiveLists().iterator(); it.hasNext();)
			{
				FloodList fl = it.next();
				
				for (Iterator<NickData> it2 = fl.getMatches().iterator(); it2.hasNext();)
				{
					NickData nd = it2.next();
					
					if (nd.getActiveListCount() < min)
						continue;
					
					if (nd.akilled)
					{
						++dupes;
						continue;
					}
					
					nd.akilled = true;
					++count;
					
					Moo.akill(nd.ip, "+2d", "Possible flood bot (" + nd.nick_str + ")");
				}
			}
			
			Moo.operwall(source + " used AKILL for " + count + " flood entries");
			
			Moo.reply(source, target, "Akilled " + count + " entries from " + lists + " lists with " + dupes + " duplicates. Removed " + purge() + " entries.");
			return;
		}
		else if (params[1].equalsIgnoreCase("CLEAR"))
		{
			int count = FloodList.getActiveLists().size();
			FloodList.clearFloodLists();
			
			Moo.reply(source, target, "Deleted " + count + " lists.");
			return;
		}
		
		int i;
		try
		{
			i = Integer.parseInt(params[1]);
		}
		catch (NumberFormatException ex)
		{
			Moo.notice(source, "Invalid flood list number");
			return;
		}
		
		FloodList fl = FloodList.getFloodListAt(i - 1);
		if (fl == null)
		{
			Moo.notice(source, "There is no flood list numbered " + i);
			return;
		}
		
		if (params[2].equalsIgnoreCase("LIST"))
		{
			int j = 1;
			for (Iterator<NickData> it = fl.getMatches().iterator(); it.hasNext();)
			{
				NickData nd = it.next();
				Moo.notice(source, j++ + ": " + nd);
			}
			Moo.notice(source, "End of flood list, " + fl.getMatches().size() + " entries");
		}
		else if (params[2].equalsIgnoreCase("DEL"))
		{
			if (params.length > 3)
			{
				if (Character.isDigit(params[3].charAt(0)) && !params[3].contains("."))
				{
					// A valid range argument would be 1-5,9,10,15. Duplicates should be allowed.
					String[] parts = params[3].split(",");
					TreeSet<Integer> tobedeleted = new TreeSet<Integer>();
					for (int k = 0; k < parts.length; k++)
					{
						int dashpos = parts[k].indexOf('-');
						
						if (dashpos == -1) // No range, just a single integer
						{
							int tmp;
							try
							{
								tmp = Integer.valueOf(parts[k]);
							}
							catch (NumberFormatException ex)
							{
								continue;
							}
							tobedeleted.add(tmp - 1);
						}
						else
						{
							int min, max;
							String lower = parts[k].substring(0, dashpos), upper = parts[k].substring(dashpos+1, parts[k].length());

							try
							{
								min = Integer.valueOf(lower);
								max = Integer.valueOf(upper);
							}
							catch (NumberFormatException ex)
							{		
								continue;
							}
							
							// Prevent memory overflow/very slow responses
							if(max > fl.getMatches().size())
								max = fl.getMatches().size();
							
							for ( ; min <= max; min++)
								tobedeleted.add(min - 1);
						}
					}
					
					if (tobedeleted.isEmpty())
					{
						Moo.reply(source, target, "Nothing to be deleted.");
					}
					else
					{
						int deleted = 0;
						int j = 0;
						for (Iterator<NickData> it = fl.getMatches().iterator(); it.hasNext(); j++)
						{
							NickData nd = it.next();
							
							if(!tobedeleted.contains(j)) // XXX: this is inefficient!
								continue;
							
							it.remove();
							fl.delMatch(nd);
							deleted++;
						}
						
						Moo.reply(source, target, "Deleted " + (fl.getMatches().isEmpty() ? "all" : deleted) + " entries");
					}
				}
				else
				{
					boolean match = false;
					for (Iterator<NickData> it = fl.getMatches().iterator(); it.hasNext();)
					{
						NickData fe = it.next();
						
						if (Moo.matches(fe.ip, params[3]))
						{
							Moo.notice(source, "Removed flood entry " + fe.ip);
							it.remove();
							fl.delMatch(fe);
							match = true;
						}
					}
					if (match == false)
						Moo.notice(source, "No match for " + params[3]);
				}
			}
			else
			{
				fl.getMatches().clear();
				Moo.reply(source, target, "Removed flood list " + i);
			}
		}
		else if (params[2].equalsIgnoreCase("AKILL"))
		{
			String duration = "2d";
			if (params.length > 3)
			{
				String dur = params[3];
				if (dur.startsWith("+"))
					dur = params[3].substring(1);
				
				if (dur.isEmpty() == false && (dur.endsWith("d") || dur.endsWith("h")
						|| dur.endsWith("m") || dur.endsWith("s")
						|| Character.isDigit(dur.charAt(dur.length() - 1))))
				{
					duration = dur;
				}
				else
				{
					Moo.notice(source, "Invalid duration: " + params[3]);
					return;
				}
			}
			
			int count = 0, duplicates = 0;
			for (Iterator<NickData> it = fl.getMatches().iterator(); it.hasNext();)
			{
				NickData fe = it.next();
				
				if (fe.akilled)
				{
					++duplicates;
					continue;
				}
				
				fe.akilled = true;
				++count;
				
				Moo.akill(fe.ip, "+" + duration, "Possible flood bot (" + fe.nick_str + ")");
			}

			Moo.operwall(source + " used AKILL for " + count + " flood entries");
			Moo.reply(source, target, "Akilled " + count + " entries with " + duplicates + " duplicates. Removed " + purge() + " entries.");
			/* purge() is guaranteed to eat this list */
			fl = null;
		}
		else if (params[2].equalsIgnoreCase("APPLY"))
		{
			if (params.length < 4)
			{
				Moo.notice(source, "Syntax: !FLOOD <flood list number> APPLY <regex>");
				return;
			}
			
			try
			{
				int fln = Integer.parseInt(params[3]);
				
				int deleted = 0;
				for (Iterator<NickData> it = fl.getMatches().iterator(); it.hasNext();)
				{
					NickData nd = it.next();
					
					if (nd.getActiveListCount() < fln)
					{
						it.remove();
						fl.delMatch(nd);
						++deleted;
					}
				}
				
				Moo.reply(source, target, "Deleted " + (fl.getMatches().isEmpty() ? "all" : deleted) + " entries");
				
				return;
			}
			catch (NumberFormatException nfe)
			{
			
				Pattern regex;
				try
				{
					regex = Pattern.compile(params[3]);
				}
				catch (PatternSyntaxException ex)
				{
					Moo.notice(source, "Invalid regex: " + params[3]);
					return;
				}
				
				int deleted = 0;
				for (Iterator<NickData> it = fl.getMatches().iterator(); it.hasNext();)
				{
					NickData nd = it.next();
					
					if (regex.matcher(nd.nick_str).matches())
						continue;
					
					it.remove();
					fl.delMatch(nd);
					
					deleted++;
				}
				
				Moo.reply(source, target, "Deleted " + (fl.getMatches().isEmpty() ? "all" : deleted) + " entries");
			}
		}
		else if (params[2].equalsIgnoreCase("APPLYW"))
		{
			if (params.length < 4)
			{
				Moo.notice(source, "Syntax: !FLOOD <flood list number> APPLYW <wildcard match>");
				return;
			}
			
			int deleted = 0;
			for (Iterator<NickData> it = fl.getMatches().iterator(); it.hasNext();)
			{
				NickData nd = it.next();
				
				if (Moo.wmatch(params[3], nd.nick_str))
					continue;
				
				it.remove();
				fl.delMatch(nd);
				
				deleted++;
			}
			
			Moo.reply(source, target, "Deleted " + (fl.getMatches().isEmpty() ? "all" : deleted) + " entries");
		}
		else if (params[2].equalsIgnoreCase("APPLYF"))
		{
			if (params.length < 4)
			{
				Moo.notice(source, "Syntax: !FLOOD <flood list number> APPLYF <nick!user@host/gecos regex>");
				return;
			}
			
			Pattern regex;
			try
			{
				regex = Pattern.compile(params[3]);
			}
			catch (PatternSyntaxException ex)
			{
				Moo.notice(source, "Invalid regex: " + params[3]);
				return;
			}

			int deleted = 0;
			for (Iterator<NickData> it = fl.getMatches().iterator(); it.hasNext();)
			{
				NickData nd = it.next();

				if (regex.matcher(nd.nick_str + "!" + nd.user_str + "@" +nd.ip + "/" + nd.realname_str).matches())
					continue;

				it.remove();
				fl.delMatch(nd);

				deleted++;
			}

			Moo.reply(source, target, "Deleted " + (fl.getMatches().isEmpty() ? "all" : deleted) + " entries");
		}
		
		if (fl != null && fl.getMatches().isEmpty() == true)
			FloodList.removeFloodListAt(i - 1);
	}
}