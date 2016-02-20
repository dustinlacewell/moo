package net.rizon.moo.irc;

import java.util.EnumSet;

public class Membership
{
	private User user;
	private Channel channel;
	private EnumSet<ChannelUserStatus> status = EnumSet.noneOf(ChannelUserStatus.class);

	public Membership(User user, Channel channel)
	{
		this.user = user;
		this.channel = channel;
	}

	public User getUser()
	{
		return this.user;
	}

	public Channel getChannel()
	{
		return this.channel;
	}

	public void addStatus(ChannelUserStatus s)
	{
		this.status.add(s);
	}

	public void removeStatus(ChannelUserStatus s)
	{
		this.status.remove(s);
	}

	public void setStatus(EnumSet<ChannelUserStatus> s)
	{
		this.status = s;
	}

	public boolean hasAnyStatus(User nick)
	{
		return !status.isEmpty();
	}

	public boolean haStatus(ChannelUserStatus s)
	{
		return status.contains(s);
	}
}
