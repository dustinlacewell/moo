package net.rizon.moo.vote;

import java.util.Date;

import net.rizon.moo.Command;
import net.rizon.moo.Mail;
import net.rizon.moo.Moo;
import net.rizon.moo.MPackage;

class commandVoteBase extends Command
{
	public commandVoteBase(MPackage pkg, final String command)
	{
		super(pkg, command, "Vote and manage votes");
	}
	
	@Override
	public void onHelp(String source)
	{
		Moo.notice(source, this.getCommandName() + " keeps track of simple yes/no votes for channels.");
		Moo.notice(source, "Syntax:");
		Moo.notice(source, this.getCommandName() + " ADD <vote info here> -- add a new vote");
		Moo.notice(source, this.getCommandName() + " CLOSE <num> -- close a vote. This cannot be undone!");
		Moo.notice(source, this.getCommandName() + " INFO <num> -- show info about a certain vote (date added, who added, votes)");
		Moo.notice(source, this.getCommandName() + " LIST [ALL] -- list known votes. If ALL is given, closed votes will also be shown");
		Moo.notice(source, this.getCommandName() + " <num> yes/no -- vote on a certain vote");
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		int e = source.indexOf('!');
		String nick = source.substring(0, e != -1 ? e : source.length());
		
		if (params.length < 2 || params[1].equalsIgnoreCase("list"))
		{
			boolean all = params.length > 2 && params[2].equalsIgnoreCase("all");
			VoteInfo[] votes = VoteInfo.getVotes(target);

			boolean any = false;
			Date date = new Date();
			for (final VoteInfo v : votes)
				if (all || v.closed == false)
				{
					Moo.reply(source, target, "[VOTE #" + v.id + "] " + v.info + " by: " + v.owner + " " + Moo.difference(date, v.date) + " ago.");
					any = true;
				}
			
			if (any == false)
			{
				Moo.reply(source, target, "No votes for " + target);
				return;
			}
		}
		else if (params[1].equalsIgnoreCase("add") && params.length > 2)
		{
			Date date = new Date();
			VoteInfo v = new VoteInfo();
			v.id = VoteInfo.getMaxFor(target);
			if (v.id == -1)
			{
				Moo.reply(source, target, "Unable to create vote!");
				return;
			}
			
			v.channel = target;
			String msg = "";
			for (int i = 2; i < params.length; ++i)
				msg += params[i] + " ";
			v.info = msg.trim();
			v.owner = nick;
			
			v.insert();
			
			Moo.reply(source, target, "Added vote #" + v.id);
			
			if (Moo.conf.getVoteEmailFor(target) != null)
				Mail.send(Moo.conf.getVoteEmailFor(target), "New vote in " + target, nick + " has added a new vote at " + date + " in " + target + ": " + v.info);
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
				Moo.reply(source, target, "Vote " + params[2] + " is not a valid number");
				return;
			}
			
			VoteInfo v = VoteInfo.getVote(vnum, target);
			if (v == null)
			{
				Moo.reply(source, target, "Vote " + vnum + " does not exist.");
				return;
			}
			
			Moo.reply(source, target, "Vote #" + vnum + " for " + target + " added by " + v.owner + " " + Moo.difference(new Date(), v.date) + " ago.");
			Moo.reply(source, target, v.info);
			
			Cast[] casts = Cast.getCastsFor(v);
			int negative = 0, positive = 0, total = 0;
			String voted = "";
			for (final Cast c : casts)
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
				Moo.reply(source, target, "Total: " + total + ", For: " + positive + " [" + (((float) positive / (float) total) * 100F) + "%], Against: " + negative + " [" + (((float) negative / (float) total) * 100F) + "%]");
				Moo.reply(source, target, "Voted: " + voted);
			}
			else
				Moo.reply(source, target, "Noone has voted yet!");
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
				Moo.reply(source, target, "Vote " + params[2] + " is not a valid number");
				return;
			}
			
			VoteInfo v = VoteInfo.getVote(vnum, target);
			if (v == null)
			{
				Moo.reply(source, target, "Vote " + vnum + " does not exist.");
				return;
			}
			
			v.close();
			Moo.reply(source, target, "Vote " + vnum + " closed.");
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
				Moo.reply(source, target, "Invalid vote number");
				return;
			}
			
			VoteInfo v = VoteInfo.getVote(vnum, target);
			if (v == null)
			{
				Moo.reply(source, target, "Error finding vote " + vnum);
				return;
			}
			
			if (v.findCastFor(nick))
			{
				Moo.reply(source, target, "You have already voted.");
				return;
			}
			
			if (v.closed)
			{
				// Can't let you do that, Starfox.
				Moo.reply(source, target, "This vote is closed.");
				return;
			}
			
			Cast c = new Cast();
			c.id = v.id;
			c.channel = v.channel;
			c.voter = nick;
			
			if (params[2].equalsIgnoreCase("yes"))
				c.vote = true;
			else if (params[2].equalsIgnoreCase("no"))
				c.vote = false;
			else
			{
				Moo.reply(source, target, "Invalid vote, use yes or no");
				return;
			}
			
			Moo.reply(source, target, "Vote cast");
			c.insert();
		}
		else
		{
			this.onHelp(source);
		}
	}
}

class CommandVote
{
	public CommandVote(MPackage pkg)
	{
		new commandVoteBase(pkg, "!MOO-VOTE");
		new commandVoteBase(pkg, "!VOTE");
	}
}