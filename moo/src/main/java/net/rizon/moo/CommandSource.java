package net.rizon.moo;

import net.rizon.moo.irc.Nameable;
import net.rizon.moo.irc.User;

public class CommandSource
{
	private User user;
	private User destUser;
	private Channel destChannel;

	public CommandSource(User user, User dest)
	{
		this.user = user;
		this.dest = dest;
	}

	public User getUser()
	{
		return user;
	}

	public String getTargetName()
	{
		return dest.getName();
	}

	public void reply(String message)
	{
		Moo.reply(user.getNick(), dest.getName(), message);
	}

	public void notice(String message)
	{
		Moo.notice(user.getNick(), message);
	}
}
