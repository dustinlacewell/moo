package net.rizon.moo;

import java.util.Collection;
import java.util.HashMap;

public class User implements Nameable
{
	private String nick;
	private final HashMap<Channel, Membership> channels = new HashMap<Channel, Membership>();

	public User(String nick)
	{
		this.nick = nick;
	}

	public String getNick()
	{
		return nick;
	}

	@Override
	public String getName()
	{
		return this.getNick();
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
