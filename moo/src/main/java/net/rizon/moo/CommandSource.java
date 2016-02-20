package net.rizon.moo;

import net.rizon.moo.irc.Channel;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.User;

public class CommandSource
{
	private final Protocol protocol;
	private final User user;
	private final Channel channel;

	public CommandSource(Protocol protocol, User user, Channel channel)
	{
		this.protocol = protocol;
		this.user = user;
		this.channel = channel;
	}

	public User getUser()
	{
		return user;
	}

	public String getTargetName()
	{
		return channel.getName();
	}

	public void reply(String message)
	{
		protocol.reply(user.getNick(), getTargetName(), message);
	}

	public void notice(String message)
	{
		protocol.notice(user.getNick(), message);
	}
}
