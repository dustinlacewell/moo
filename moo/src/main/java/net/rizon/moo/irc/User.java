package net.rizon.moo.irc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class User
{
	private String nick;
	private final Map<Channel, Membership> channels = new HashMap<>();

	public User(String nick)
	{
		this.nick = nick;
	}

	public String getNick()
	{
		return nick;
	}

	public void setNick(String nick)
	{
		this.nick = nick;
	}

	public void addChannel(Membership mem)
	{
		assert mem.getUser() == this;
		this.channels.put(mem.getChannel(), mem);
	}

	public void removeChannel(Membership mem)
	{
		assert mem.getUser() == this;
		this.channels.remove(mem.getChannel());
	}

	public Collection<Membership> getChannels()
	{
		return channels.values();
	}

	public Membership findChannel(Channel c)
	{
		return channels.get(c);
	}
}
