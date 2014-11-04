package net.rizon.moo.plugin.vote;

import net.rizon.moo.Channel;
import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Mail;
import net.rizon.moo.Membership;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.User;

import java.util.Collection;
import java.util.Date;

class commandVoteBase extends Command
{
	public commandVoteBase(Plugin pkg, final String command)
	{
		super(pkg, command, "Vote and manage votes");
		
		this.requiresChannel(Moo.conf.staff_channels);
		this.requiresChannel(Moo.conf.oper_channels);
		this.requiresChannel(Moo.conf.admin_channels);
	}
	
	@Override
	public void onHelp(CommandSource source)
	{
		source.notice(this.getCommandName() + " keeps track of simple yes/no votes for channels.");
		source.notice("Syntax:");
		source.notice(this.getCommandName() + " ADD <vote info here> -- add a new vote");
		source.notice(this.getCommandName() + " CLOSE <num> -- close a vote. This cannot be undone!");
		source.notice(this.getCommandName() + " INFO <num> -- show info about a certain vote (date added, who added, votes)");
		source.notice(this.getCommandName() + " LIST [ALL] -- list known votes. If ALL is given, closed votes will also be shown");
		source.notice(this.getCommandName() + " <num> yes/no -- vote on a certain vote");
	}
	
	@Override
	public void execute(CommandSource source, String[] params)
	{
		String nick = source.getUser().getNick();
		String target = source.getTargetName();
		
		if (params.length < 2 || params[1].equalsIgnoreCase("list"))
		{
			boolean all = params.length > 2 && params[2].equalsIgnoreCase("all");
			VoteInfo[] votes = VoteInfo.getVotes(target);

			boolean any = false;
			Date date = new Date();
			for (final VoteInfo v : votes)
				if (all || v.closed == false)
				{
					String msg = "[VOTE #" + v.id + "] " + v.info + " by: " + v.owner + " " + Moo.difference(date, v.date) + " ago.";
					if (all)
						Moo.notice(target, msg);
					else
						source.reply("[VOTE #" + v.id + "] " + v.info + " by: " + v.owner + " " + Moo.difference(date, v.date) + " ago.");
					any = true;
				}
			
			if (any == false)
			{
				source.reply("No votes for " + target);
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
				source.reply("Unable to create vote!");
				return;
			}
			
			v.channel = target;
			String msg = "";
			for (int i = 2; i < params.length; ++i)
				msg += params[i] + " ";
			v.info = msg.trim();
			v.owner = nick;
			
			v.insert();

			source.reply("Added vote #" + v.id);
			
			String email = vote.getVoteEmailFor(target);
			if (email != null)
				Mail.send(email, "New vote in " + target, nick + " has added a new vote at " + date + " in " + target + ": " + v.info);
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
				source.reply("Vote " + params[2] + " is not a valid number");
				return;
			}
			
			VoteInfo v = VoteInfo.getVote(vnum, target);
			if (v == null)
			{
				source.reply("Vote " + vnum + " does not exist.");
				return;
			}

			source.reply("Vote #" + vnum + " for " + target + " added by " + v.owner + " " + Moo.difference(new Date(), v.date) + " ago.");
			source.reply(v.info);
			
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
				source.reply("Total: " + total + ", For: " + positive + " [" + (((float) positive / (float) total) * 100F) + "%], Against: " + negative + " [" + (((float) negative / (float) total) * 100F) + "%]");
				source.reply("Voted: " + voted);
			}
			else
				source.reply("Noone has voted yet!");
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
				source.reply("Vote " + params[2] + " is not a valid number");
				return;
			}
			
			VoteInfo v = VoteInfo.getVote(vnum, target);
			if (v == null)
			{
				source.reply("Vote " + vnum + " does not exist.");
				return;
			}
			
			v.close();
			source.reply("Vote " + vnum + " closed.");
		}
		else if (params[1].equalsIgnoreCase("slackers") && params.length > 2)
		{
			/* !vote slackers is *intentionally* undocumented in the help output because there's inevitably going to be
			 * that one idiot who can't contain himself and spam it in #opers forever and ever until the end of time.
			 */
			int vnum;
			try
			{
				vnum = Integer.parseInt(params[2]);
			}
			catch (NumberFormatException ex)
			{
				source.reply("Vote " + params[2] + " is not a valid number");
				return;
			}

			VoteInfo v = VoteInfo.getVote(vnum, target);
			if (v == null)
			{
				source.reply("Vote " + vnum + " does not exist.");
				return;
			}

			Channel c = Moo.channels.find(target);
			Collection<Membership> users = c.getUsers();
			StringBuilder sb = new StringBuilder("People who need to vote on vote #");
			sb.append(vnum);
			sb.append(":");
			int listed = 0;
			for (Membership mem : users)
			{
				User u = mem.getUser();

				if (v.findCastFor(u.getNick()) || u == Moo.me)
					continue;

				listed++;

				sb.append(' ');
				sb.append(u.getNick());

				/* 512 - ":" (1) - NICKLEN (30 on Rizon) - "!" (1) - USERLEN (10) - "@" (1) - HOSTLEN (63) - " :" (2)
				 *     - "People who need to vote on vote #" (33) - ": " (2) - "\r\n" (2) = 367
				 *
				 * Cutting down to 360; I doubt we'll ever have votes with more than 7 digits.
				 */
				if (sb.length() > 360)
				{
					source.reply(sb.toString());
					sb = new StringBuilder("People who need to vote on vote #");
					sb.append(vnum);
					sb.append(":");
					listed = 0;
				}
			}
			if (listed > 0)
				source.reply(sb.toString());
			else
				source.reply("Nobody is slacking off on voting for a change.");
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
				source.reply("Invalid vote number");
				return;
			}
			
			VoteInfo v = VoteInfo.getVote(vnum, target);
			if (v == null)
			{
				source.reply("Error finding vote " + vnum);
				return;
			}
			
			if (v.findCastFor(nick))
			{
				source.reply("You have already voted.");
				return;
			}
			
			if (v.closed)
			{
				// Can't let you do that, Starfox.
				source.reply("This vote is closed.");
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
				source.reply("Invalid vote, use yes or no");
				return;
			}

			source.reply("Vote cast");
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
	private Command mv, v;
	
	public CommandVote(Plugin pkg)
	{
		mv = new commandVoteBase(pkg, "!MOO-VOTE");
		v = new commandVoteBase(pkg, "!VOTE");
	}
	
	public void remove()
	{
		mv.remove();
		v.remove();
	}
}