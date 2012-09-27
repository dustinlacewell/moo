package net.rizon.moo.vote;

import java.util.Date;

import net.rizon.moo.command;
import net.rizon.moo.mail;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

class commandVoteBase extends command
{
	public commandVoteBase(mpackage pkg, final String command)
	{
		super(pkg, command, "Vote and manage votes");
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
		moo.notice(source, this.getCommandName() + " keeps track of simple yes/no votes for channels.");
		moo.notice(source, "Syntax:");
		moo.notice(source, this.getCommandName() + " ADD <vote info here> -- add a new vote");
		moo.notice(source, this.getCommandName() + " CLOSE <num> -- close a vote. This cannot be undone!");
		moo.notice(source, this.getCommandName() + " INFO <num> -- show info about a certain vote (date added, who added, votes)");
		moo.notice(source, this.getCommandName() + " LIST [ALL] -- list known votes. If ALL is given, closed votes will also be shown");
		moo.notice(source, this.getCommandName() + " <num> yes/no -- vote on a certain vote");
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		int e = source.indexOf('!');
		String nick = source.substring(0, e != -1 ? e : source.length());
		
		if (params.length < 2 || params[1].equalsIgnoreCase("list"))
		{
			boolean all = params.length > 2 && params[2].equalsIgnoreCase("all");
			voteinfo[] votes = voteinfo.getVotes(target);

			boolean any = false;
			Date date = new Date();
			for (final voteinfo v : votes)
				if (all || v.closed == false)
				{
					moo.reply(source, target, "[VOTE #" + v.id + "] " + v.info + " by: " + v.owner + " " + difference(date, v.date) + " ago.");
					any = true;
				}
			
			if (any == false)
			{
				moo.reply(source, target, "No votes for " + target);
				return;
			}
		}
		else if (params[1].equalsIgnoreCase("add") && params.length > 2)
		{
			Date date = new Date();
			voteinfo v = new voteinfo();
			v.id = voteinfo.getMaxFor(target);
			if (v.id == -1)
			{
				moo.reply(source, target, "Unable to create vote!");
				return;
			}
			
			v.channel = target;
			String msg = "";
			for (int i = 2; i < params.length; ++i)
				msg += params[i] + " ";
			v.info = msg.trim();
			v.owner = nick;
			
			v.insert();
			
			moo.reply(source, target, "Added vote #" + v.id);
			
			if (moo.conf.getVoteEmailFor(target) != null)
				mail.send(moo.conf.getVoteEmailFor(target), "New vote in " + target, nick + " has added a new vote at " + date + " in " + target + ": " + v.info);
		}
		else if (params[1].equalsIgnoreCase("info") && params.length > 2)
		{
			int vnum;
			try
			{
				vnum = Integer.parseInt(params[2]);
			}
			catch (NumberFormatException ex)
			{
				moo.reply(source, target, "Vote " + params[2] + " is not a valid number");
				return;
			}
			
			voteinfo v = voteinfo.getVote(vnum, target);
			if (v == null)
			{
				moo.reply(source, target, "Vote " + vnum + " does not exist.");
				return;
			}
			
			moo.reply(source, target, "Vote #" + vnum + " for " + target + " added by " + v.owner + " " + difference(new Date(), v.date) + " ago.");
			moo.reply(source, target, v.info);
			
			cast[] casts = cast.getCastsFor(v);
			int negative = 0, positive = 0, total = 0;
			String voted = "";
			for (final cast c : casts)
			{
				if (c.vote)
					++positive;
				else
					++negative;
				++total;
				
				voted += c.voter + " ";
			}
			
			if (total > 0)
			{
				moo.reply(source, target, "Total: " + total + ", For: " + positive + " [" + (((float) positive / (float) total) * 100F) + "%], Against: " + negative + " [" + (((float) negative / (float) total) * 100F) + "%]");
				moo.reply(source, target, "Voted: " + voted);
			}
			else
				moo.reply(source, target, "Noone has voted yet!");
		}
		else if (params[1].equalsIgnoreCase("close") && params.length > 2)
		{
			int vnum;
			try
			{
				vnum = Integer.parseInt(params[2]);
			}
			catch (NumberFormatException ex)
			{
				moo.reply(source, target, "Vote " + params[2] + " is not a valid number");
				return;
			}
			
			voteinfo v = voteinfo.getVote(vnum, target);
			if (v == null)
			{
				moo.reply(source, target, "Vote " + vnum + " does not exist.");
				return;
			}
			
			v.close();
			moo.reply(source, target, "Vote " + vnum + " closed.");
		}
		else if (params.length > 2)
		{
			int vnum;
			try
			{
				vnum = Integer.parseInt(params[1]);
			}
			catch (NumberFormatException ex)
			{
				moo.reply(source, target, "Invalid vote number");
				return;
			}
			
			voteinfo v = voteinfo.getVote(vnum, target);
			if (v == null)
			{
				moo.reply(source, target, "Error finding vote " + vnum);
				return;
			}
			
			if (v.findCastFor(nick))
			{
				moo.reply(source, target, "You have already voted.");
				return;
			}
			
			if (v.closed)
			{
				// Can't let you do that, Starfox.
				moo.reply(source, target, "This vote is closed.");
				return;
			}
			
			cast c = new cast();
			c.id = v.id;
			c.channel = v.channel;
			c.voter = nick;
			
			if (params[2].equalsIgnoreCase("yes"))
				c.vote = true;
			else if (params[2].equalsIgnoreCase("no"))
				c.vote = false;
			else
			{
				moo.reply(source, target, "Invalid vote, use yes or no");
				return;
			}
			
			moo.reply(source, target, "Vote cast");
			c.insert();
		}
		else
		{
			this.onHelp(source);
		}
	}
}

class commandVote
{
	public commandVote(mpackage pkg)
	{
		new commandVoteBase(pkg, "!MOO-VOTE");
		new commandVoteBase(pkg, "!VOTE");
	}
}