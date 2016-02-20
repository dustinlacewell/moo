package net.rizon.moo;

import net.rizon.moo.irc.Channel;
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
		this.destUser = dest;
	}

	public CommandSource(User user, Channel channel)
	{
		this.user = user;
		this.destChannel = channel;
	}

	public User getUser()
	{
		return user;
	}

	public String getTargetName()
	{
		return destUser != null ? destUser.getNick() : destChannel.getName();
	}

	public void reply(String message)
	{
		Moo.reply(user.getNick(), getTargetName(), message);
	}

	public void notice(String message)
	{
		Moo.notice(user.getNick(), message);
	}
}
